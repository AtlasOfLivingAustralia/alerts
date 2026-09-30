import { defineConfig, devices } from '@playwright/test';
import dotenv from 'dotenv';
import path from 'node:path';

// Credentials live in e2e/.env (gitignored). In CI they come from the CI secret store.
dotenv.config({ path: path.join(__dirname, '.env') });

/**
 * The app must already be running: `./gradlew bootRun` from the project root serves
 * http://localhost:8080. Override with BASE_URL=... npm test
 */
export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  fullyParallel: true,
  retries: 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: process.env.BASE_URL ?? 'http://localhost:8080',
    // Traces can capture typed credentials, so keep them off unless retrying.
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    ignoreHTTPSErrors: true,
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
});
