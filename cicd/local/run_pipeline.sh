#!/usr/bin/env bash
#
# Run the alerts CI/CD flow locally against a minikube cluster.
#
# Same order as the AWS pipeline (cicd/backend/pipeline/*): build, deploy, then test.
#
#   pipeline stage / action        local equivalent (this script)
#   -----------------------------  ---------------------------------------------------
#   ExportConfig (helm-values)     cicd/local/values-minikube.yaml (static)
#   BuildAlerts                    build:    gradlew bootWar + docker build into minikube
#   DeployAlerts                   deploy:   mysql + helm upgrade --install (+ migration)
#   RunE2ETests                    test:     Playwright against the pod just deployed
#   TeardownAlerts                 teardown: helm uninstall + namespace delete
#
# About the post-deploy tests. Every spec signs in through the Cognito hosted UI, which
# only redirects back to a registered callback URL, so the suite can run against the
# local pod only once that callback exists:
#
#   1. add http://localhost:8080/callback?client_name=OidcClient to the Cognito app
#      client's callback URLs (plain http is allowed for localhost), then
#   2. deploy with that client:
#      OIDC_CLIENT_ID=... OIDC_CLIENT_SECRET=... cicd/local/run_pipeline.sh deploy
#
# Until then `test` verifies the deployed pod is healthy, explains why the suite was not
# run against it, and exits 0. To run the suite against the shared testing environment
# instead (still after the deploy), set E2E_BASE_URL=https://alerts.test.ala.org.au.
#
# Not reproduced locally: ECR, IRSA service accounts (eksctl), ALB ingress/ACM/S3 access
# logs, Secrets Manager, Aurora snapshots and Backtrack.
#
# Usage:
#   cicd/local/run_pipeline.sh all          # build -> deploy -> test
#   cicd/local/run_pipeline.sh build
#   cicd/local/run_pipeline.sh deploy
#   cicd/local/run_pipeline.sh test         # post-deploy Playwright run
#   cicd/local/run_pipeline.sh pretest      # optional pre-deploy run against a remote env
#   cicd/local/run_pipeline.sh status | logs | port-forward | teardown
#
# Environment overrides:
#   MINIKUBE_PROFILE   minikube profile                (default: minikube)
#   NAMESPACE          kubernetes namespace            (default: alerts-local)
#   RELEASE_NAME       helm release name               (default: ala-alerts-local)
#   IMAGE_TAG          image tag for both images       (default: local)
#   LOCAL_PORT         port-forward port for the app   (default: 8080)
#   E2E_BASE_URL       post-deploy test target         (default: http://localhost:$LOCAL_PORT)
#   E2E_ARGS           extra playwright args           (e.g. "tests/alerts.spec.ts")
#   E2E_PASSWORD_SECRET_ID  AWS Secrets Manager secret ID (password is read by login.ts)
#   PRETEST_BASE_URL   target for the optional pre-deploy run
#                           (default: https://alerts.test.ala.org.au)
#   SKIP_TESTS         1 to stop after the deploy
#   SKIP_GRADLE        1 to reuse an existing build/libs/*.war

set -euo pipefail

MINIKUBE_PROFILE="${MINIKUBE_PROFILE:-minikube}"
NAMESPACE="${NAMESPACE:-alerts-local}"
RELEASE_NAME="${RELEASE_NAME:-ala-alerts-local}"
IMAGE_TAG="${IMAGE_TAG:-local}"
LOCAL_PORT="${LOCAL_PORT:-8080}"
E2E_BASE_URL="${E2E_BASE_URL:-http://localhost:$LOCAL_PORT}"
PRETEST_BASE_URL="${PRETEST_BASE_URL:-https://alerts.test.ala.org.au}"
E2E_ARGS="${E2E_ARGS:-}"
SKIP_TESTS="${SKIP_TESTS:-0}"
SKIP_GRADLE="${SKIP_GRADLE:-0}"

APP_IMAGE="alerts:${IMAGE_TAG}"
LIQUIBASE_IMAGE="alerts-liquibase:${IMAGE_TAG}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
VALUES_FILE="$SCRIPT_DIR/values-minikube.yaml"

log() { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }
warn() { printf '\033[1;33mWARNING: %s\033[0m\n' "$*" >&2; }
die() { printf '\033[1;31mERROR: %s\033[0m\n' "$*" >&2; exit 1; }

preflight() {
  log "Checking tooling"
  for bin in minikube kubectl helm docker; do
    command -v "$bin" >/dev/null 2>&1 || die "$bin is not on PATH"
  done
  minikube -p "$MINIKUBE_PROFILE" status >/dev/null 2>&1 \
    || die "minikube profile '$MINIKUBE_PROFILE' is not running (minikube start -p $MINIKUBE_PROFILE)"
  kubectl config use-context "$MINIKUBE_PROFILE" >/dev/null
  echo "minikube profile : $MINIKUBE_PROFILE"
  echo "kube context     : $(kubectl config current-context)"
  echo "namespace        : $NAMESPACE"
  echo "helm release     : $RELEASE_NAME"
  echo "app image        : $APP_IMAGE"
  echo "liquibase image  : $LIQUIBASE_IMAGE"
  echo "test target      : $E2E_BASE_URL"
}

ensure_namespace() {
  kubectl get namespace "$NAMESPACE" >/dev/null 2>&1 \
    || kubectl create namespace "$NAMESPACE" >/dev/null
}

run_playwright() {
  local base_url="$1"
  shift
  command -v npm >/dev/null 2>&1 || die "npm is not on PATH (needed for Playwright)"
  (
    cd "$REPO_ROOT/e2e"
    [ -d node_modules ] || npm ci
    npx playwright install chromium
    # login.ts gets the password directly from the macOS Keychain, or from Secrets Manager
    # when E2E_PASSWORD_SECRET_ID is set. The password is never placed in the environment.
    # shellcheck disable=SC2086
    BASE_URL="$base_url" npx playwright test --retries=1 "$@" $E2E_ARGS
  )
}

# Image delivery without a registry (there is no ECR locally).
#
#   docker runtime     -> build straight into minikube's docker daemon (docker-env)
#   containerd/cri-o   -> build on the host, then `minikube image load`
#
# `minikube docker-env` does not just fail on a containerd cluster, it can block, so the
# runtime is detected from the node rather than discovered by trying.
node_container_runtime() {
  kubectl get nodes -o jsonpath='{.items[0].status.nodeInfo.containerRuntimeVersion}' 2>/dev/null \
    | cut -d: -f1
}

build_images() {
  preflight

  if [ "$SKIP_GRADLE" != "1" ]; then
    log "Building the war (BuildAlerts: ./gradlew bootWar)"
    (cd "$REPO_ROOT" && ./gradlew bootWar)
  else
    log "SKIP_GRADLE=1, reusing build/libs"
  fi
  rm -f "$REPO_ROOT"/build/libs/*-plain.war
  ls "$REPO_ROOT"/build/libs/*.war >/dev/null 2>&1 || die "no war in build/libs"

  local runtime load_images=1
  runtime="$(node_container_runtime)"
  echo "node container runtime: ${runtime:-unknown}"
  if [ "$runtime" = "docker" ]; then
    if eval "$(minikube -p "$MINIKUBE_PROFILE" docker-env)"; then
      load_images=0
      echo "building inside minikube's docker daemon"
    else
      warn "could not use minikube's docker daemon; building on the host instead"
    fi
  fi

  log "Building $APP_IMAGE"
  docker build -f "$REPO_ROOT/Dockerfile" -t "$APP_IMAGE" "$REPO_ROOT"

  log "Building $LIQUIBASE_IMAGE"
  docker build -f "$REPO_ROOT/docker/liquibase/Dockerfile" -t "$LIQUIBASE_IMAGE" "$REPO_ROOT"

  if [ "$load_images" = "1" ]; then
    log "Loading the images into minikube ($runtime runtime)"
    minikube -p "$MINIKUBE_PROFILE" image load "$APP_IMAGE"
    minikube -p "$MINIKUBE_PROFILE" image load "$LIQUIBASE_IMAGE"
  fi
}

deploy_mysql() {
  log "Deploying local MySQL (stands in for Aurora)"
  ensure_namespace
  kubectl -n "$NAMESPACE" apply -f "$SCRIPT_DIR/mysql.yaml"
  kubectl -n "$NAMESPACE" rollout status deployment/mysql-local --timeout=5m
}

deploy_app() {
  preflight
  deploy_mysql

  # A previous run that was interrupted while helm was waiting leaves the release in
  # pending-install/pending-upgrade, which blocks every later `helm upgrade`.
  # `|| true` because `helm status` exits non-zero (and pipefail propagates it) when the
  # release does not exist yet, which is the normal first-install case.
  local state=""
  state=$( { helm status "$RELEASE_NAME" -n "$NAMESPACE" -o json 2>/dev/null || true; } \
    | sed -n 's/.*"status":"\(pending-[a-z]*\)".*/\1/p' | head -1)
  if [ -n "$state" ]; then
    warn "release is stuck in $state (interrupted run); uninstalling it first"
    helm uninstall "$RELEASE_NAME" -n "$NAMESPACE" >/dev/null || true
  fi

  local oidc_overrides=()
  if [ -n "${OIDC_CLIENT_ID:-}" ]; then
    oidc_overrides+=(--set-string "config.cognito.oidc.alerts_client_id=$OIDC_CLIENT_ID")
  fi
  if [ -n "${OIDC_CLIENT_SECRET:-}" ]; then
    oidc_overrides+=(--set-string "config.cognito.oidc.alerts_client_secret=$OIDC_CLIENT_SECRET")
  fi

  log "helm upgrade --install (DeployAlerts)"
  helm upgrade --install "$RELEASE_NAME" "$REPO_ROOT/helm" \
    -n "$NAMESPACE" \
    -f "$VALUES_FILE" \
    --set "image.repository=alerts" \
    --set "image.tag=$IMAGE_TAG" \
    --set-string "migration.image.repository=alerts-liquibase" \
    --set-string "migration.image.tag=$IMAGE_TAG" \
    --set "rds.externalName=mysql-local.$NAMESPACE.svc.cluster.local" \
    --set-string "migration.startedAt=$(date -u '+%Y-%m-%dT%H:%M:%SZ')" \
    "${oidc_overrides[@]+"${oidc_overrides[@]}"}" \
    --wait --wait-for-jobs --timeout 15m

  helm status "$RELEASE_NAME" -n "$NAMESPACE"
}

start_port_forward() {
  # The chart names the Service "svc-<fullname>", so look it up by label rather than
  # guessing.
  local svc
  svc=$(kubectl -n "$NAMESPACE" get svc \
    -l "app.kubernetes.io/instance=$RELEASE_NAME" \
    -o jsonpath='{range .items[?(@.spec.type=="ClusterIP")]}{.metadata.name}{"\n"}{end}' | head -1)
  [ -n "$svc" ] || die "no ClusterIP service found for release $RELEASE_NAME in $NAMESPACE"

  kubectl -n "$NAMESPACE" port-forward "svc/$svc" "$LOCAL_PORT:80" >/tmp/alerts-pf.log 2>&1 &
  PF_PID=$!
  trap 'kill "$PF_PID" >/dev/null 2>&1 || true' EXIT
  for _ in $(seq 1 45); do
    if curl -fsS "http://localhost:$LOCAL_PORT/actuator/health" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  warn "the app did not answer on http://localhost:$LOCAL_PORT within 90s"
  cat /tmp/alerts-pf.log >&2 || true
  return 1
}

# True when the deployed release has a real OIDC client, i.e. one that can complete a
# login. With the placeholder from values-minikube.yaml the hosted UI rejects the
# request, so the specs that sign in cannot run against the local pod.
local_login_is_configured() {
  local client_id
  client_id=$( { helm get values "$RELEASE_NAME" -n "$NAMESPACE" -o json 2>/dev/null || true; } \
    | sed -n 's/.*"alerts_client_id":"\([^"]*\)".*/\1/p' | head -1)
  [ -n "$client_id" ] && [ "$client_id" != "local-placeholder" ]
}

target_is_local() {
  case "$E2E_BASE_URL" in
    http://localhost:*|http://127.0.0.1:*) return 0 ;;
    *) return 1 ;;
  esac
}

# Brings up whatever the tests need to reach $E2E_BASE_URL, and fails if it is not healthy.
prepare_test_target() {
  if target_is_local; then
    # Nothing is reachable on localhost until the port-forward is up; waiting for
    # /actuator/health here is the readiness check the pipeline gets from the ALB.
    start_port_forward || {
      echo "The deployed app did not become healthy. Inspect with:"
      echo "  cicd/local/run_pipeline.sh logs"
      echo "Roll back with:"
      echo "  helm rollback $RELEASE_NAME -n $NAMESPACE"
      return 1
    }
    echo "Health OK: $E2E_BASE_URL/actuator/health"
  elif ! curl -fsS -o /dev/null --max-time 20 "$E2E_BASE_URL"; then
    die "$E2E_BASE_URL is not reachable."
  fi
}

rollback_hint() {
  echo "Report: e2e/playwright-report/index.html (npm --prefix e2e run report)"
  if target_is_local; then
    echo "Roll the local release back with:"
    echo "  helm rollback $RELEASE_NAME -n $NAMESPACE"
  fi
}

# ---------------------------------------------------------------------------
# RunE2ETests: the post-deploy test stage, in the same position as in the pipeline.
# Target is $E2E_BASE_URL, which defaults to the pod that was just deployed.
# ---------------------------------------------------------------------------
run_tests() {
  if [ "$SKIP_TESTS" = "1" ]; then
    log "SKIP_TESTS=1, stopping after the deploy"
    return 0
  fi

  preflight
  log "Post-deploy tests against $E2E_BASE_URL"
  prepare_test_target || return 1

  # The specs sign in, so they need a callback registered for the host under test.
  if target_is_local && ! local_login_is_configured; then
    cat <<MSG

The deployed pod is healthy, but the Playwright suite was not run against it: every spec
signs in through the Cognito hosted UI, and this release uses the placeholder OIDC
client, so the login would be rejected.

To run the suite against the local pod:
  1. add this callback URL to the Cognito app client (plain http is allowed for localhost):
       http://localhost:$LOCAL_PORT/callback?client_name=OidcClient
  2. redeploy with that client, then test again:
       OIDC_CLIENT_ID=... OIDC_CLIENT_SECRET=... cicd/local/run_pipeline.sh deploy
       cicd/local/run_pipeline.sh test

Or run the suite against the shared testing environment instead:
  E2E_BASE_URL=$PRETEST_BASE_URL cicd/local/run_pipeline.sh test
MSG
    return 0
  fi

  log "Running the Playwright suite against $E2E_BASE_URL"
  run_playwright "$E2E_BASE_URL" || { rollback_hint; return 1; }
  echo "Post-deploy tests passed against $E2E_BASE_URL."
}


# Optional: run the suite against a *remote* environment before deploying locally, e.g.
# to confirm the shared testing environment is healthy first. Not part of `all`.
run_pretest() {
  log "Pre-deploy run against $PRETEST_BASE_URL"
  command -v npm >/dev/null 2>&1 || die "npm is not on PATH (needed for Playwright)"

  curl -fsS -o /dev/null --max-time 20 "$PRETEST_BASE_URL" \
    || die "$PRETEST_BASE_URL is not reachable."

  run_playwright "$PRETEST_BASE_URL" \
    || die "the suite failed against $PRETEST_BASE_URL.
Report: e2e/playwright-report/index.html (npm --prefix e2e run report)"

  echo "Passed against $PRETEST_BASE_URL."
}

show_status() {
  preflight
  helm list -n "$NAMESPACE" || true
  kubectl -n "$NAMESPACE" get pods,svc,jobs
  echo
  echo "test password   : read from macOS Keychain (or AWS Secrets Manager when E2E_PASSWORD_SECRET_ID is set)"
  if local_login_is_configured; then
    echo "local login     : a real OIDC client is deployed, the suite can run against the local pod"
  else
    echo "local login     : placeholder OIDC client, the suite cannot log in to the local pod"
  fi
}

show_logs() {
  kubectl -n "$NAMESPACE" logs \
    -l "app.kubernetes.io/instance=$RELEASE_NAME,app.kubernetes.io/component!=database-migration" \
    --tail=200 -f
}

teardown() {
  log "Teardown (TeardownAlerts)"
  helm uninstall "$RELEASE_NAME" -n "$NAMESPACE" || true
  kubectl delete namespace "$NAMESPACE" --ignore-not-found
}

case "${1:-all}" in
  preflight) preflight ;;
  build) build_images ;;
  mysql) deploy_mysql ;;
  deploy) deploy_app ;;
  test) run_tests ;;
  pretest) run_pretest ;;
  status) show_status ;;
  logs) show_logs ;;
  port-forward)
    preflight
    start_port_forward && { echo "Forwarding http://localhost:$LOCAL_PORT - Ctrl-C to stop"; wait "$PF_PID"; }
    ;;
  teardown) teardown ;;
  all)
    build_images
    deploy_app
    run_tests
    ;;
  *) die "unknown command '$1' (build|deploy|test|pretest|status|logs|port-forward|teardown|all)" ;;
esac

