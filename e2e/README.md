# Playwright end-to-end tests

Browser tests for the alerts app. Tests that need authentication call
`login(page, username)`, so a test account is required before they can run.

## Setup

```bash
cd e2e
npm install
npm run install:browsers
cp .env.example .env      # gitignored - configure explicit test usernames
```

### Credentials

Every test passes its username explicitly to `login(page, username)`. `.env` contains only
non-secret usernames and `E2E_BASE_URL`; it never contains a password.

`login.ts` obtains the matching password at login time:

- **Local macOS:** from the Keychain item with service `alerts-e2e` and account equal to the
  username passed by the test. Store each account's password with:
  ```bash
  security add-generic-password -U -s alerts-e2e -a "<username>" -w
  ```
- **AWS CodeBuild:** directly from the field matching the username in the Secrets Manager
  secret identified by `E2E_AWS_SECRET_ID`. The buildspec passes the secret ID, not the
  password; `login.ts` calls AWS CLI when a test needs the password.

In CI, `E2E_USERNAME`, `E2E_BIOSECURITY_USERNAME`, and `E2E_STANDARD_USERNAME` are
passed to Playwright from `cicd/backend/config.ini`. The admin tests use `E2E_USERNAME`,
biosecurity tests use `E2E_BIOSECURITY_USERNAME`, and standard-user tests use
`E2E_STANDARD_USERNAME`. Local `.env` can provide the same three values.

## Running in the AWS pipeline (post-deployment gate)

The suite runs in its own pipeline stage, `Post_Deployment_Tests` (action `RunE2ETests`,
buildspec `cicd/backend/pipeline/e2e_tests_buildspec.yaml`), *after* the `DeployAlerts`
action has deployed the new version:

1. `DeployAlerts` — `helm upgrade --set replicaCount=1 --atomic` (preceded by the Liquibase
   migration when there is one), then `verify_release_deployed`; the Helm release must report
   `deployed`.
2. `RunE2ETests` — `npm ci`, `npx playwright install --with-deps chromium`, then
   `npx playwright test --retries=1` with `E2E_BASE_URL=https://$DOMAIN_NAME`
3. A failing test fails the `RunE2ETests` CodeBuild action; this stage does not tear down,
   roll back, or otherwise modify the deployment.

There is no blue/green slot in the Helm chart (single Deployment, single Service and Ingress on
`$DOMAIN_NAME`), so the tests run against the deployed version receiving live traffic.

### When it runs

The gate is tied to the git branch, not to a per-environment switch. `SRC_BRANCH` is checked in
`cicd/backend/pipeline/export_config_buildspec.yaml`:

| Branch | Environment | Gate |
| --- | --- | --- |
| `testing` | testing | **runs** |
| feature branches | development | skipped |
| `main` / `master` | staging | skipped |
| `main` / `master` (`--env prod`) | production | skipped |

So only a deploy of the `testing` branch runs the tests.

### Credentials

`E2E_USERNAME` is in `cicd/backend/config.ini` (a dedicated Cognito test account for the testing
environment — not a personal or admin login). `SECRET_NAME` comes from the base stack's
`AlertsSecret` output and is passed to Playwright as `E2E_AWS_SECRET_ID`. Each test passes
its username to `login`; the login code fetches that username's key directly from Secrets
Manager. The password is not passed through CodePipeline variables or a process environment
variable. A missing username, AWS permission, or secret key makes the test action fail.

The Playwright HTML report is published in the `RunE2ETests` build artifacts under
`playwright-report/`.

A CI job runs the suite with:

```bash
cd e2e && npm ci && npx playwright install --with-deps chromium && npm test
```

### Test localhost or running tests against a different base URL
## Boot up server

Start the app from the project root first (`./gradlew bootRun`), then:

## Run tests

```bash
cd e2e
npm test                                   # all tests
npx playwright test tests/alerts.spec.ts   # one file
# or headless:
# npm run test:headless tests/alerts.spec.ts 
npm run report                             # last HTML report
npm test                                   #run all tests against the local host with headless Chrome
# or use a different base URL:
# E2E_BASE_URL=https://alerts.test.ala.org.au npm test
# or with headed Chrome:
# E2E_BASE_URL=https://alerts.test.ala.org.au npm run test:headed 
# or run a single test:
# E2E_BASE_URL=https://alerts.test.ala.org.au npm run test -- biosecurity.spec.ts --grep "add user to a random alert"
```

## Debugging

```bash
npm run test:debug -- tests/alerts.spec.ts:21   # Playwright Inspector, step by step
npm run test:ui                                 # watch mode + time travel
npm run test:headed                             # watch it in a real browser
```

In IntelliJ, click the gutter ▶/🐞 next to a test; breakpoints work in both the spec and
`login.ts`. `await page.pause()` also opens the Inspector at that point.

## Layout

- `playwright.config.ts` – base URL, `.env` loading, one `chromium` project.
- `login.ts` – `login(page, username)`: retrieves that account's password and signs in through the Cognito hosted UI.
- `tests/alerts.spec.ts` – standard and custom alerts on /notification/myAlerts.
- `tests/admin.spec.ts` – admin pages.
- `tests/biosecurity.spec.ts` – biosecurity pages.
