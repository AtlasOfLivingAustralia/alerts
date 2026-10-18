# Running the pipeline locally on minikube
## CI/CD TESTING PURPOSE ONLY
## DO NOT RUN IN NORMAL OPERATION

The AWS buildspecs in `cicd/backend/pipeline/` cannot be run as-is on a laptop: they
assume CodeBuild/CodePipeline (`$CODEBUILD_SRC_DIR_*` artifacts, pipeline namespace
variables), ECR, CloudFormation stack outputs, Secrets Manager, EKS + IRSA service
accounts created with `eksctl`, an ALB ingress with an ACM certificate, and Aurora
snapshots/Backtrack.

`cicd/local/run_pipeline.sh` runs the same **sequence of steps**, in the same order, against
minikube: build, deploy, then test.

| Pipeline stage / action | Local equivalent |
| --- | --- |
| `ExportConfig` (generates `helm-values.yaml`) | static `cicd/local/values-minikube.yaml` |
| `BuildAlerts` (gradle + buildx push to ECR) | `build` — `./gradlew bootWar` + `docker build` into minikube's image store |
| `DeployAlerts` (eksctl IRSA + `helm upgrade --install`) | `deploy` — local MySQL (`cicd/local/mysql.yaml`) + `helm upgrade --install` with the Liquibase migration hook |
| `RunE2ETests` | `test` — waits for `/actuator/health` on the deployed pod, then runs the Playwright suite against it |
| `TeardownAlerts` | `teardown` — `helm uninstall` + namespace delete |

## Quick start

```bash
minikube start --cpus 4 --memory 8192     # the app requests 1Gi and heaps to 2g
cicd/local/run_pipeline.sh all            # build -> deploy -> test
cicd/local/run_pipeline.sh port-forward   # http://localhost:8080
cicd/local/run_pipeline.sh logs
cicd/local/run_pipeline.sh teardown
```

Individual steps: `build`, `mysql`, `deploy`, `test`, `status`. `SKIP_TESTS=1` stops after
the deploy; `SKIP_GRADLE=1` reuses the existing war.

## The post-deploy tests run against the local pod

`test` targets the pod that was just deployed (`http://localhost:8080` through a
port-forward), waits for it to report healthy — the local stand-in for the ALB health
check the pipeline gets for free — and runs the Playwright suite against it.

### Logging in to the local pod

Every spec calls `login()`, which goes through the Cognito hosted UI, and Cognito only
redirects back to callback URLs **registered on the app client**. The client used for
local development (`grails run-app` on `http://localhost:8080`) already has that callback,
so `deploy` reuses it: the id/secret are read from the local-dev config file,
`/data/alerts/config/alerts-config.yml` (`security.oidc.clientId` / `secret`, plus the
server-to-server pair from `webservice.client-id` / `client-secret`). Nothing to
configure if you already run the app locally.

Overrides:

```bash
LOCAL_DEV_CONFIG=~/other/alerts-config.yml cicd/local/run_pipeline.sh deploy
OIDC_CLIENT_ID=... OIDC_CLIENT_SECRET=...  cicd/local/run_pipeline.sh deploy
```

Without any of those the chart's **placeholder** client is deployed (pac4j refuses to
start with a blank one). `test` then reports the pod healthy, explains that the suite
cannot log in, and exits 0 rather than failing on a known limitation. `status` shows
which situation you are in (`local login: …`).

### ConfigMaps the chart expects

`helm/templates/deployment.yaml` mounts `logback.xml`, `logback.groovy` and
`system-message.json` from three ConfigMaps it does **not** create (`optional: true`); on
EKS they are provisioned outside the chart. "Optional" is a trap: when the ConfigMap is
absent Kubernetes still creates the `subPath` mount — as an empty *directory* at the
file's path — and every page then 500s with `system-message.json (Is a directory)` while
the pod stays Running and healthy. `deploy` applies `cicd/local/configmaps.yaml` first to
avoid that. To show a banner on the local pod, edit `system-message.json` there:
`{"text": "Local minikube deployment", "severity": "info"}`.

### Which specs pass locally

| Spec | Local pod | Needs |
| --- | --- | --- |
| `admin.spec.ts` | ✅ 3 passed | admin role on the test account |
| `alerts.spec.ts` | ✅ passed | — |
| `biosecurity.dryrun.spec.ts` | ✅ passed (1 skipped by design) | — |
| `biosecurity.spec.ts` | ❌ 5 fail | **seeded data**: biosecurity alerts, a species list id, CSV reports |

The `biosecurity.spec.ts` failures are data, not deployment: the local MySQL is a fresh
Liquibase schema with the bootstrap queries only (`0` biosecurity alerts, `0` result
rows), while the testing database has the alerts the spec clicks through. Until the
local DB is seeded, run the suite without that file:

```bash
E2E_ARGS="tests/admin.spec.ts tests/alerts.spec.ts tests/biosecurity.dryrun.spec.ts" \
  cicd/local/run_pipeline.sh test
```

Or point `test` at another environment that has the data:

```bash
E2E_BASE_URL=https://alerts.test.ala.org.au cicd/local/run_pipeline.sh test
```

There is also an optional `pretest` command that runs the suite against a remote
environment (`PRETEST_BASE_URL`, default `https://alerts.test.ala.org.au`) *before* you
deploy locally. It is not part of `all`.

### Credentials for the tests

Each spec passes its username explicitly to `login(page, username)`. Local usernames are
configured in `e2e/.env`: admin tests use `E2E_USERNAME`, alerts tests use
`E2E_COMMON_USERNAME` (falling back to `E2E_USERNAME`), and biosecurity tests use
`E2E_BIOSECURITY_USERNAME` (also falling back to `E2E_USERNAME`).

The browser runs on your Mac; passwords are not stored in minikube, `.env`, or process
environment variables.

For local tests, store a password in the macOS Keychain for each account under service
`alerts-e2e` and the exact username passed by the test:

```bash
security add-generic-password -U -s alerts-e2e -a "alerts-test@example.org" -w
```

`login.ts` retrieves that account's password directly with `security find-generic-password`
when the test logs in.

To use the same AWS Secrets Manager secret as the CodeBuild pipeline instead, set its
secret ID. The Playwright login code invokes AWS CLI directly and extracts the
`e2e-password` key without exporting the password to the process environment:

```bash
E2E_PASSWORD_SECRET_ID=alerts-testing cicd/local/run_pipeline.sh test
```

This requires valid AWS credentials and `secretsmanager:GetSecretValue` permission. The
minikube cluster and app pod never receive the e2e password. When this variable is not set,
the local macOS Keychain is used.

Useful variations:

```bash
E2E_ARGS="tests/alerts.spec.ts" cicd/local/run_pipeline.sh test      # one spec
E2E_BASE_URL=https://alerts.test.ala.org.au cicd/local/run_pipeline.sh test
SKIP_TESTS=1 cicd/local/run_pipeline.sh all                          # stop after the deploy
npm --prefix e2e run report                                          # last HTML report
```

## Overrides

| Variable | Default | Purpose |
| --- | --- | --- |
| `MINIKUBE_PROFILE` | `minikube` | minikube profile / kube context |
| `NAMESPACE` | `alerts-local` | namespace to deploy into |
| `RELEASE_NAME` | `ala-alerts-local` | helm release |
| `IMAGE_TAG` | `local` | tag for both images |
| `LOCAL_PORT` | `8080` | port-forward port |
| `E2E_BASE_URL` | `http://localhost:$LOCAL_PORT` | post-deploy test target |
| `E2E_ARGS` | — | extra `playwright test` arguments |
| `E2E_PASSWORD_SECRET_ID` | — | AWS Secrets Manager secret ID; login.ts retrieves its `e2e-password` value directly |
| `PRETEST_BASE_URL` | `https://alerts.test.ala.org.au` | target for the optional `pretest` |
| `LOCAL_DEV_CONFIG` | `/data/alerts/config/alerts-config.yml` | local-dev config the OIDC client is read from for `deploy` |
| `OIDC_CLIENT_ID` / `OIDC_CLIENT_SECRET` | from `LOCAL_DEV_CONFIG` | override the OIDC client for `deploy` |
| `OIDC_WS_CLIENT_ID` / `OIDC_WS_CLIENT_SECRET` | from `LOCAL_DEV_CONFIG` | override the server-to-server client |
| `SKIP_TESTS` | `0` | `1` stops after the deploy |
| `SKIP_GRADLE` | `0` | `1` reuses the existing `build/libs/*.war` |

## What the local values change

`cicd/local/values-minikube.yaml` mirrors the **testing** environment — the same values
`export_config_buildspec.yaml` builds from `gen_env_vars.py --env testing`
(`cicd/backend/config.ini` `[testing]` over `[DEFAULT]`), which is what
`https://alerts.test.ala.org.au` runs with. So biocache, collectory, lists, BIE, the
common header/footer, userdetails, the API-key service and the CAS/Cognito endpoints all
point at the `*.test.ala.org.au` stack. Regenerate the reference list any time with:

```bash
python3 cicd/gen_env_vars.py --env testing --clean-branch testing --conf cicd/backend/config.ini
```

Everything else is identical to testing. The deliberate deltas (each marked
`LOCAL DELTA` in the file):

| Setting | testing | local | Why |
| --- | --- | --- | --- |
| `ingress` | ALB + ACM cert + S3 access logs | disabled | no ALB controller; use the port-forward |
| `serviceAccount.name` | IRSA account from `eksctl` | `default` | no IAM roles for service accounts |
| `rds.*` | Aurora writer endpoint, db `alertstesting`, user `admin`, SSL on | `mysql-local` service, db `alerts`, user `alerts_user`, SSL off | no Aurora; matches `docker/docker-compose.yml` |
| `config.base.alerts_base_url` | `https://alerts.test.ala.org.au` | `http://localhost:8080` | reached through the port-forward |
| `config.mail.*` | true (SES via IRSA) | false | no AWS credentials in the pod |
| `config.csv.*` | S3 upload | local filesystem | same |
| `cognito.cookie.*` | shared cookie on `.ala.org.au` | disabled | localhost cannot set it |
| `cognito.oidc.alerts_client_id/secret` | real client from the Cognito stack | placeholders in the file; `deploy` overrides them with the local-dev client (see above) | pac4j aborts on a blank clientId |
| `podEnvironment` | `test` | `development` | the `test` block in `application.yml` hardcodes `serverURL: http://alerts.test.ala.org.au`, which would redirect every local request away from the port-forward; `development` uses `http://localhost:8080` |
| CPU/memory + heap | 1 CPU / 4096Mi, 3g heap | 250m–2 CPU / 1–3Gi, 2048m heap | fits a laptop |


## Images: no registry involved

There is no ECR locally. `build` detects the node's container runtime
(`kubectl get nodes -o jsonpath='{...containerRuntimeVersion}'`):

- **docker** runtime → builds directly inside minikube's daemon via `docker-env`
- **containerd / cri-o** → builds on the host, then `minikube image load`

(`minikube docker-env` can *block* on a containerd cluster, which is why the runtime is
detected rather than discovered by trying.) `image.pullPolicy` is `IfNotPresent`, so the
`alerts:local` / `alerts-liquibase:local` tags are never pulled from a registry.

## Caveats

- The deploy uses `--wait --wait-for-jobs` like the pipeline, so a broken Liquibase
  changelog fails the step the same way it would in CodeBuild.
- pac4j fetches the Cognito discovery document while the context starts, so the pod needs
  outbound internet. If the pod network is not ready yet the first attempt dies with
  `Error getting URL resource` and Kubernetes restarts it — one restart on a fresh deploy
  is normal and `--wait` absorbs it.
- If you Ctrl-C during `helm ... --wait`, the release is left in `pending-install` and
  blocks later upgrades; `deploy` detects that and uninstalls it first.
- `docker build` here is single-arch; the pipeline uses `buildx` for amd64 + arm64.
- MySQL data lives in a PVC, so `teardown` (namespace delete) wipes the database. Use
  `helm uninstall` alone to keep it.
- No Aurora snapshot/Backtrack locally: if a migration goes wrong, drop the namespace and
  redeploy.

## Verified run

```text
build   : alerts:local + alerts-liquibase:local loaded into minikube (containerd)
deploy  : OIDC client 2687ef… read from /data/alerts/config/alerts-config.yml
          migration Job Complete, ala-alerts-local pod 1/1 Running
test    : http://localhost:8080  admin + alerts + biosecurity.dryrun -> 5 passed, 1 skipped (1.3m)
          biosecurity.spec.ts -> 5 failed: needs seeded biosecurity data (see above)
```


