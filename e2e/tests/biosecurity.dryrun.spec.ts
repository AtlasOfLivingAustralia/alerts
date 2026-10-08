import {expect, test} from "@playwright/test";
import {login} from "../login";

// Acacia-test
const listId = 'dr22805';
const memberEmail = "alerts-test-c@example.org";
const alertName = "Biosecurity alert for Acacia-test"

test ('test dry-run on Acacia-test', async ({ page }) => {
    await login(page);
    await page.goto('/biosecurity');
    await page.waitForURL('**/biosecurity');
    await page.waitForTimeout(1000);

    let alertCards = page.locator('div[data-name="alert-card"]');
    await expect(alertCards.first()).toBeVisible();

    let quickEntry = page.locator('div[data-name="quick-entry"]').first();
    await quickEntry.scrollIntoViewIfNeeded();
    let listIdInput = quickEntry.locator('input[name="listid"]').first();
    let userEmails = quickEntry.locator('input[name="useremails"]').first();
    let submitQuickEntryBtn = quickEntry.locator('button#quick-submit').first();
    await listIdInput.fill(listId);
    await userEmails.fill(memberEmail);
    await expect(listIdInput).toHaveValue(listId);
    await expect(userEmails).toHaveValue(memberEmail);
    await submitQuickEntryBtn.click();

    await page.waitForTimeout(1000);
    let alertDiv = page.locator('div[data-name="alert-card"]').first();
    await expect(alertDiv).toBeVisible();
    let alertNameLink = alertDiv.locator('a[data-name="alert-name"]').first();
    await expect(alertNameLink).toHaveText(alertName);

    await page.goto('/admin')
    await expect(page).toHaveURL(/\/admin\/?$/);
    await page.waitForTimeout(1000);
    const biosecurityDiv = page.locator('div[data-name="biosecurity"]').first();
    const selectDaysBefore = biosecurityDiv.locator('select#daysBefore').first();
    await selectDaysBefore.scrollIntoViewIfNeeded();
    await selectDaysBefore.selectOption('30');

    const dryRunLink = biosecurityDiv.locator('a#dryRunBiosecurityLink').first();
    await dryRunLink.scrollIntoViewIfNeeded();
    // The selected period must be reflected in the link before it is followed.
    await expect(dryRunLink).toHaveAttribute('href', /daysBefore=30/);

    // target="_blank", so the dry run opens in a popup. Start waiting for it BEFORE the
    // click, and wait for it only once - a second waitForEvent('popup') would hang forever.
    const [popup] = await Promise.all([page.waitForEvent('popup', { timeout: 30000 }), dryRunLink.click()]);
    await popup.waitForLoadState();

    expect(popup.url()).toContain('/biosecurity/dryRun?daysBefore=30');

    // The action renders JSON, so read it from the popup body.
    const body = JSON.parse(await popup.locator('body').innerText());
    expect(body.success).toBe(true);
})

