import { execFileSync } from 'node:child_process';
import { expect, Page } from '@playwright/test';

/**
 * Signs in to the alerts app.
 *
 * The app delegates authentication to AWS Cognito (security.oidc in
 * grails-app/conf/application.yml), so the credentials go into the Cognito hosted UI.
 *
 * The caller supplies the username explicitly. Passwords are read directly from the
 * macOS Keychain locally, or from the AWS Secrets Manager secret selected by
 * E2E_AWS_SECRET_ID in CI. Passwords are never read from a process environment
 * variable:
 *   security add-generic-password -U -s alerts-e2e -a "<username>" -w
 */
export async function login(page: Page, username: string): Promise<void> {
  const credentials = getCredentials(username);

  // Any protected URL triggers the redirect to the identity provider.
  await page.goto('/');

  // Cognito ships a second, hidden copy of the form and visible social sign-in buttons,
  // so everything is scoped to the visible username/password form.
  const form = page.locator('form:has(input[name="password"]:visible)').first();

  const usernameField = form.locator('input[name="username"]:visible').first();
  await expect(usernameField, 'expected to land on the login page').toBeVisible({ timeout: 30_000 });
  await usernameField.fill(credentials.username);
  await form.locator('input[name="password"]:visible').first().fill(credentials.password);
  await form.locator('input[type="submit"]:visible, button[type="submit"]:visible').first().click();

  const error = page.locator('#loginErrorMessage, .errorMessage').filter({ hasText: /\S/ }).first();
  await Promise.race([
    page.waitForURL(/\/notification\/myAlerts/i, { timeout: 30_000 }).catch(() => undefined),
    error.waitFor({ state: 'visible', timeout: 30_000 }).catch(() => undefined),
  ]);

  if (!/\/notification\/myAlerts/i.test(page.url())) {
    const message = (await error.textContent().catch(() => null))?.trim();
    throw new Error(`Login failed for "${credentials.username}"${message ? `: ${message}` : ''}`);
  }
}

function getCredentials(username: string): { username: string; password: string } {
  const account = username.trim();
  if (!account) throw new Error('login(page, username) requires an explicit username');

  const password = getPassword(account);
  if (!password) {
    throw new Error(
      `No password for "${account}". Store it in the macOS Keychain:\n` +
        `  security add-generic-password -U -s alerts-e2e -a "${account}" -w\n` +
        `or set E2E_AWS_SECRET_ID to the AWS Secrets Manager secret containing the\n` +
        `"${account}" key (the AWS pipeline supplies this secret name).`,
    );
  }
  return { username: account, password };
}

function getPassword(account: string): string | undefined {
  const secretId = process.env.E2E_AWS_SECRET_ID?.trim();
  if (secretId) {
    return awsSecretPassword(secretId, account);
  }

  if (process.platform === 'darwin') {
    return keychainPassword(account);
  }
  return undefined;
}

let cachedAwsPassword: { secretId: string; password: string } | undefined;

function awsSecretPassword(secretId: string, account: string): string {
  if (cachedAwsPassword?.secretId === secretId) return cachedAwsPassword.password;

  let secretString: string;
  try {
    secretString = execFileSync(
      'aws',
      [
        'secretsmanager',
        'get-secret-value',
        '--secret-id',
        secretId,
        '--query',
        'SecretString',
        '--output',
        'text',
      ],
      { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] },
    ).trim();
  } catch {
    throw new Error(
      `Could not read Secrets Manager secret "${secretId}". Check AWS credentials and ` +
        `secretsmanager:GetSecretValue permission.`,
    );
  }

  try {
    const secret = JSON.parse(secretString) as Record<string, unknown>;
    const password = secret[account];
    if (typeof password === 'string' && password.length > 0) {
      cachedAwsPassword = { secretId, password };
      return password;
    }
  } catch {
    // Report a safe, actionable error without printing the secret payload.
  }
  throw new Error(`Secrets Manager secret "${secretId}" has no non-empty "${account}" key.`);
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

