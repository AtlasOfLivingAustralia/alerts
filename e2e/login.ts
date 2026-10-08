import { execFileSync } from 'node:child_process';
import { expect, Page } from '@playwright/test';

/**
 * Signs in to the alerts app.
 *
 * The app delegates authentication to AWS Cognito (security.oidc in
 * grails-app/conf/application.yml), so the credentials go into the Cognito hosted UI.
 *
 * Credentials come from E2E_USERNAME / E2E_PASSWORD (see e2e/.env, which is gitignored),
 * falling back to the macOS Keychain when E2E_PASSWORD is empty:
 *   security add-generic-password -U -s alerts-e2e -a "<username>" -w
 */
export async function login(page: Page): Promise<void> {
  const { username, password } = getCredentials();

  // Any protected URL triggers the redirect to the identity provider.
  await page.goto('/');

  // Cognito ships a second, hidden copy of the form and visible social sign-in buttons,
  // so everything is scoped to the visible username/password form.
  const form = page.locator('form:has(input[name="password"]:visible)').first();

  const usernameField = form.locator('input[name="username"]:visible').first();
  await expect(usernameField, 'expected to land on the login page').toBeVisible({ timeout: 30_000 });
  await usernameField.fill(username);
  await form.locator('input[name="password"]:visible').first().fill(password);
  await form.locator('input[type="submit"]:visible, button[type="submit"]:visible').first().click();

  const error = page.locator('#loginErrorMessage, .errorMessage').filter({ hasText: /\S/ }).first();
  await Promise.race([
    page.waitForURL(/\/notification\/myAlerts/i, { timeout: 30_000 }).catch(() => undefined),
    error.waitFor({ state: 'visible', timeout: 30_000 }).catch(() => undefined),
  ]);

  if (!/\/notification\/myAlerts/i.test(page.url())) {
    const message = (await error.textContent().catch(() => null))?.trim();
    throw new Error(`Login failed for "${username}"${message ? `: ${message}` : ''}`);
  }
}

function getCredentials(): { username: string; password: string } {
  const username = process.env.E2E_USERNAME?.trim();
  if (!username) throw new Error('Missing E2E_USERNAME - copy e2e/.env.example to e2e/.env');

  const password = process.env.E2E_PASSWORD || getPassword(username);
  if (!password) {
    throw new Error(
      `No password for "${username}". Set E2E_PASSWORD in e2e/.env, store it in the macOS Keychain:\n` +
        `  security add-generic-password -U -s alerts-e2e -a "${username}" -w\n` +
        `or, on Travis, define ALERTS_TEST_PASSWORD in the repository settings.`,
    );
  }
  return { username, password };
}

function getPassword(account: string): string | undefined {
  if (process.env.TRAVIS === 'true') {
    return process.env.ALERTS_TEST_PASSWORD;
  }

  if (process.platform === 'darwin') {
    return keychainPassword(account);
  }
  return undefined;
}

function keychainPassword(account: string): string | undefined {
  if (process.platform !== 'darwin') return undefined;
  try {
    const args = ['find-generic-password', '-s', 'alerts-e2e', '-a', account, '-w'];
    return execFileSync('/usr/bin/security', args, {
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
    }).trim();
  } catch {
    return undefined;
  }
}

