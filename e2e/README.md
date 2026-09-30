# Playwright end-to-end tests

Browser tests for the alerts app. Every test calls `login()` first, so a test account is
required before anything can run.

## Setup

```bash
cd e2e
npm install
npm run install:browsers
cp .env.example .env      # gitignored - put the test account details here
```

### Credentials

Never commit a username/password. `login.ts` reads `E2E_USERNAME` / `E2E_PASSWORD` from:

- **`e2e/.env`** locally (gitignored; `.env.example` is the tracked template), or
- the **macOS Keychain** when `E2E_PASSWORD` is empty:
  ```bash
  security add-generic-password -U -s alerts-e2e -a "<username>" -w   # prompts, nothing echoed
  ```
- **CI secrets** - on Travis the password comes from `ALERTS_TEST_PASSWORD` (see below).

Use a dedicated, low-privilege test account.

#### Storing them in Travis

Nothing secret goes into the repository. `login.ts` detects Travis (`TRAVIS === 'true'`) and
reads the password from **`ALERTS_TEST_PASSWORD`**, which is defined in the Travis settings:

1. Travis → the repository → **More options → Settings**
2. **Environment Variables** → Name: `ALERTS_TEST_PASSWORD`, Value: the test account password
3. Leave **Display value in build log** switched **OFF**, and limit it to the branch that runs
   the tests if you don't need it everywhere.

Same thing from the command line:

```bash
gem install travis                 # or: brew install travis
travis login --com                 # --org for legacy travis-ci.org

travis env set ALERTS_TEST_PASSWORD '<the password>' --private --com   # --private = hidden in logs
travis env list --com                                                  # names only, values stay hidden
```

The username is not a secret, so it can live in `.travis.yml` (or alongside the password in
Settings):

```yaml
env:
  global:
    - E2E_USERNAME=alerts-test@example.org
```

To rotate the password, set the variable again - the new value replaces the old one and no
commit is required.

Two caveats:

- Settings variables are **not** exposed to builds from forked pull requests, so e2e tests
  there will fail on the missing password - guard the job with `if: fork = false`, or skip it
  when `ALERTS_TEST_PASSWORD` is empty.
- Travis masks the value in the build log, but anything your own code prints is not masked -
  never `echo "$ALERTS_TEST_PASSWORD"`.

#### Other CI systems

## Not implemented or will not be implemented
- Our current CI/CD pipeline needs Travis build and pass tests ahead and then deploy to AWS. However, Travis cannot run the tests before the new code is deployed to AWS.

- **AWS CodeBuild** (`cicd/`): Secrets Manager or SSM Parameter Store via `env/secrets-manager`
  in the buildspec.

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
npm run report                             # last HTML report
npm test                                   #run all tests against the local host with headless Chrome
# or use a different base URL:
# BASE_URL=https://alerts.test.ala.org.au npm test
# or with headed Chrome:
# BASE_URL=https://alerts.test.ala.org.au npm run test:headed 
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
- `login.ts` – `login(page)`: resolves the credentials and signs in through the Cognito hosted UI.
- `tests/alerts.spec.ts` – standard and custom alerts on /notification/myAlerts.
- `tests/admin.spec.ts` – admin pages.
- `tests/biosecurity.spec.ts` – biosecurity pages.
