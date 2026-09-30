import { expect, test } from '@playwright/test';
import { login } from '../login';

/**
 * The alerts panel (grails-app/views/notification/_alertsPanel.gsp) has the tabs
 * "Standard Alerts" (#standard-alerts) and "Custom Alerts" (#custom-alerts) - the tab
 * buttons carry role="tab". Each alert is a .list-group-item with an <h5> name,
 * a <p> description and an on/off switch.
 */
const STANDARD_ALERTS = [
  { name: 'Blogs and News', description: 'Notify me when ALA Blog items are added.' },
  { name: 'My Annotations', description: 'Notify me when records I have flagged are updated.' },
  { name: 'Annotations', description: 'Notify me when annotations are made on any record.' },
  { name: 'New records', description: 'Notify me when new records are added.' },
  { name: 'New images', description: 'Notify me when new images are added.' },
  { name: 'Spatial layers', description: 'Notify me when new spatial layers are added.' },
  { name: 'New occurrence datasets', description: 'Notify me when new occurrence datasets are added.' },
  { name: 'Species lists', description: 'Notify me when new species lists are added.' },
];

test('test standard alerts (on/off)', async ({ page }) => {
  await login(page);

  const items = page.locator('#standard-alerts .list-group-item');

  for (const { name, description } of STANDARD_ALERTS) {
    // "contains": other standard alerts may also be configured for this environment.
    const item = items.filter({ hasText: description }).first();

    await expect(item, `standard alert "${name}"`).toBeVisible();
    await expect(item.locator('h5')).toHaveText(name);
    // Each alert can be switched on/off.
    await expect(item.locator('input[type="checkbox"][role="switch"]')).toBeVisible();
  }

  let names = ["Blogs and News", "My Annotations", "Annotations", "New records", "New images", "Spatial layers", "New occurrence datasets", "Species lists"];
  for (const name of names) {
    //find h5 elements contain 'My Annotations'
    const alertInput = items.locator(`input[type="checkbox"][role="switch"][name="${name.replace(/\s+/g, '_')}"]`);
    await alertInput.scrollIntoViewIfNeeded();
    await expect(alertInput).toBeVisible();
    let currentAlertValue =await alertInput.isChecked();
    //click the input element
    await alertInput.click();
    //wait for 1 second to let the server complete the request
    await page.waitForTimeout(1000);
    //refresh the page
    await page.reload();
    await alertInput.scrollIntoViewIfNeeded();
    await expect(alertInput).toBeVisible();
    const expectedValue = await alertInput.isChecked();
    expect(
        expectedValue,
        `Unexpected value for ${name} switch: expected ${!currentAlertValue}, but got ${expectedValue}`
    ).toBe(!currentAlertValue);
  }
});

/**
 * Custom alerts are user-dependent, so we combine the alerts creation api testset with the custom alerts testset.
 * The custom alerts are created in the api testset, and then we check if they are displayed in the custom alerts tab.
 */
test('test custom alerts (on/off/create/delete)', async ({ page }) => {
  await login(page);
  // go to /ws/test, find links of alerts creation and click
  await page.goto('/ws/test');
  const links = page.locator('ul[name="createAlerts"] a');
  const linkCount = await links.count();

  for (let i = 0; i < linkCount; i++) {
    await page.goto('/ws/test');
    const link =  page.locator('ul[name="createAlerts"] a').nth(i);
    const title = await link.getAttribute('title');
    await link.click();
    await page.waitForURL('**/notification/myAlerts#custom-alerts');
    await expect(page.locator('#custom-alerts h5').filter({hasText: title})).toBeVisible();
    const alertInput = page.locator('#custom-alerts .list-group-item').filter({ hasText: title }).locator('input[type="checkbox"][role="switch"]');
    await alertInput.scrollIntoViewIfNeeded();
    let currentAlertValue =await alertInput.isChecked();
    await alertInput.click();
    //wait for 1 second to let the server complete the request
    await page.waitForTimeout(1000);
    //refresh the page
    await page.reload();
    await alertInput.scrollIntoViewIfNeeded();
    await expect(alertInput).toBeVisible();
    const expectedValue = await alertInput.isChecked();
    expect(
        expectedValue,
        `Unexpected value for ${title} switch: expected ${!currentAlertValue}, but got ${expectedValue}`
    ).toBe(!currentAlertValue);
    //delete the alert
    const deleteButton = page.locator('#custom-alerts .list-group-item').filter({ hasText: title }).locator('.fa.fa-trash');
    await deleteButton.scrollIntoViewIfNeeded();
    await deleteButton.click();
    await page.waitForURL('**/notification/myAlerts#custom-alerts');
    await page.waitForTimeout(1000);
    await expect(page.locator('#custom-alerts h5').filter({ hasText: title })).not.toBeVisible();
  }
});
