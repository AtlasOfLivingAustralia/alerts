import { expect, test } from '@playwright/test';
import { login } from '../login';

const memberEmail = "alerts-test-c@example.org";

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

test ("admin function - schedule test", async ({ page }) => {
    await login(page);
    const adminLink = page.locator('a.btn').filter({ hasText: 'Admin' }).first();
    await adminLink.click();
    await page.waitForURL('**/admin');
    const scheduleLink = page.locator('a').filter({ hasText: 'View scheduling ' }).first();
    await scheduleLink.click();
    await page.waitForURL('**/quartz/index');

    const jobs = ["BiosecurityJob", "EmailUpdateJob", "DailyJob", "WeeklyJob", "MonthlyJob", "HourlyJob"];
    for (const jobName of jobs) {
        const job = page.locator(`tr#${jobName}`).first();
        const pauseAction = await job.locator('td[data-name="action"] a').filter({ hasText: 'Pause' });
        const resumeAction = await job.locator('td[data-name="action"] a').filter({ hasText: 'Resume' });

        await resumeAction.scrollIntoViewIfNeeded();
        await resumeAction.click();
        let jobStatus = await job.locator('td[data-name="state"] span').innerText();
        await expect(jobStatus).toBe('NORMAL');

        await pauseAction.scrollIntoViewIfNeeded();
        await pauseAction.click();
        jobStatus = await job.locator('td[data-name="state"] span').innerText();
        await expect(jobStatus).toBe('PAUSED');
    }
});

/**
 * Use user alatest@yahoo.com as the test user.
 * make sure the user is already created in the system before running this test.
 */
test ("admin function - user management (view and deletion)", async ({ page }) => {
    await login(page);
    const adminLink = page.locator('a.btn').filter({ hasText: 'Admin' }).first();
    await adminLink.click();
    await page.waitForURL('**/admin');
    const userManagementLink = page.locator('a#user-management').first();
    await userManagementLink.click();
    await page.waitForURL('**/admin/user');
    const userForm = page.locator('form[name="find-user-form"]');

    const userInput = userForm.locator('input#term');
    await userInput.pressSequentially(memberEmail);
    //wait for 1 second to let the server complete the autocomplete request and display the selectable user
    await page.waitForTimeout(1000);
    const selectableUser = page
        .locator('#term_listbox div[role="option"].tt-selectable')
        .filter({ hasText: memberEmail });

    // if the user is found, click it and check its subscriptions, and then delete it.
    let isDeleted = false;
    if (await selectableUser.isVisible()) {
        await selectableUser.click();
        await page.waitForURL('**/admin/user/**');

        const title = page.locator('h2').filter({ hasText: `${memberEmail} email subscriptions` }).first();
        expect(title).toBeVisible();

        const items = page.locator('#standard-alerts .list-group-item');
        for (const { name, description } of STANDARD_ALERTS) {
            // "contains": other standard alerts may also be configured for this environment.
            const item = items.filter({ hasText: description }).first();

            await expect(item, `standard alert "${name}"`).toBeVisible();
            await expect(item.locator('h5')).toHaveText(name);
            // Each alert can be switched on/off.
            await expect(item.locator('input[type="checkbox"][role="switch"]')).toBeVisible();
        }

        const deleteBtn = page.locator('[name="delete-user-btn"]');
        await deleteBtn.click();
        const modal = page.locator('#deleteUserModal');
        await expect(modal).toBeVisible();
        const deleteConfirmBtn = modal.locator('button#confirmDeleteUserBtn');
        await deleteConfirmBtn.scrollIntoViewIfNeeded();
        page.once('dialog', async dialog => {
            expect(dialog.type()).toBe('alert');
            await dialog.accept(); // Clicks "OK/Yes"
        });
        await deleteConfirmBtn.click();
        await page.waitForURL('**/admin/user');
        isDeleted = true;
    }

    if (!isDeleted) {
        //if the user is not found, the add user form will be displayed.
        const addUserBtn = page.locator('button[name="add-user-btn"]');
        await addUserBtn.isVisible()
        // wait for the js dialog to appear,and then accept it
        page.once('dialog', async dialog => {
            expect(dialog.type()).toBe('confirm');
            await dialog.accept(); // Clicks "OK/Yes"
        });
        await addUserBtn.click();
        await page.waitForURL('**/admin/user/**');
        const title = page.locator('h2').filter({hasText: `${memberEmail} email subscriptions`}).first();
        expect(title).toBeVisible();
    } else {
        // if the user is already deleted, need to retype the email to add it again.
        //userInput = userForm.locator('input#term');
        userInput.pressSequentially(memberEmail);
        //wait for 1 second to let the server complete the autocomplete request and display the selectable user
        await page.waitForTimeout(1000);

        const addUserBtn = page.locator('button[name="add-user-btn"]');
        await addUserBtn.isVisible()
        // wait for the js dialog to appear,and then accept it
        page.once('dialog', async dialog => {
            expect(dialog.type()).toBe('confirm');
            await dialog.accept(); // Clicks "OK/Yes"
        });
        await addUserBtn.click();
    }
})

test ("admin function - query updates", async ({ page }) => {
    await login(page);
    await page.goto('/query');
    const title = page.locator('h3').filter({ hasText: 'Query List' });
    expect(title).toBeVisible();
    const table = page.locator('table#queries');

    // collect all tr elements in the table
    const rows = await table.locator('tr').all();
    //randomly select one row
    const randomRow = rows[Math.floor(Math.random() * rows.length)];
    //select the first column of the randomly selected row, it is an ID
    const randomRowId = await randomRow.locator('td:nth-child(1)').innerText();
    // find the link this row and click it
    const randomRowLink = await randomRow.locator('a');
    await randomRowLink.click();
    await page.waitForURL(`**/query/show/${randomRowId}`);

    // find a button with the text "Edit"
    const editBtn = await page.locator('a.btn').filter({ hasText: 'Edit' });
    await editBtn.scrollIntoViewIfNeeded();
    await editBtn.click();
    await page.waitForURL(`**/query/edit/${randomRowId}`);
   // find an input , value is Update
    const updateBtn = await page.locator('.btn').filter({ hasText: 'Update' });
    await updateBtn.scrollIntoViewIfNeeded();
    await updateBtn.click();
    await page.waitForURL(`**/query/show/${randomRowId}`);
})
