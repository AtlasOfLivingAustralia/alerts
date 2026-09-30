import { expect, test } from '@playwright/test';
import { login } from '../login';

const memberEmail = "alerts-test-c@example.org";
const listId = "dr22916";
const alertName = "Biosecurity alert for IPA-test"

test ('biosecurity schedule tests', async ({ page }) => {
    await login(page);
    await page.goto('/biosecurity');
    await page.waitForURL('**/biosecurity');
    await page.waitForTimeout(1000);
    let statusInfo = await page.locator('div[name="statusInfo"]').innerText();
    expect(
        statusInfo.includes('Next run will be on') ||
        statusInfo.includes('Warning: Alerts are PAUSED')
    ).toBe(true);

    let scheduleBtn = page.locator('button#showScheduleBtn').first();
    await scheduleBtn.scrollIntoViewIfNeeded();
    await scheduleBtn.click();
    await page.waitForTimeout(1000);
    expect(await scheduleBtn.innerText()).toBe('Hide Schedule Manager');
    await page.waitForSelector('div#rescheduleBiosecurity', { state: 'visible' });

    const pauseBtn = page.locator('button[name="pauseBtn"]').first();
    const resumeBtn = page.locator('button[name="resumeBtn"]').first();
    const saveScheduleBtn = page.locator('button[name="saveScheduleBtn"]').first();
    const cancelScheduleBtn = page.locator('button[name="cancelScheduleBtn"]').first();
    const updateScheduleBtn = page.locator('button[name="updateScheduleBtn"]').first();
    const addMonitoringTeamMemberBtn = page.locator('[data-name="add-monitoring-team-member-btn"]').first();

    await pauseBtn.scrollIntoViewIfNeeded();
    await pauseBtn.click();
    await page.waitForTimeout(1000);
    statusInfo = await page.locator('div[name="statusInfo"]').innerText();
    expect(statusInfo.includes('Warning: Alerts are PAUSED')).toBe(true);

    await resumeBtn.scrollIntoViewIfNeeded();
    await resumeBtn.click();
    await page.waitForTimeout(1000);
    statusInfo = await page.locator('div[name="statusInfo"]').innerText();
    expect(statusInfo.includes('Next run will be on')).toBe(true);

    await saveScheduleBtn.scrollIntoViewIfNeeded();
    await saveScheduleBtn.click();
    await page.waitForTimeout(1000);

    let pauseResumeInfo = await page.locator('div[name="pauseResumeInfo"]');
    expect(await pauseResumeInfo.isVisible()).toBe(true);
    // get today date, convert it to : Wednesday, 30 Sept 2026, 0:00
    const today = new Date();
    const date = today.toLocaleDateString('en-US', {
        weekday: 'long',
        month: 'short',
        day: 'numeric',
        year: 'numeric'
    });
    const formattedDate = `${date}`;

    expect(await pauseResumeInfo.innerText()).toContain(`Alerts are scheduled to pause on ${formattedDate}`);
    expect(await pauseResumeInfo.innerText()).toContain(`and resume on ${formattedDate}`);

    await cancelScheduleBtn.scrollIntoViewIfNeeded();
    await cancelScheduleBtn.click();
    await page.waitForTimeout(1000);
    await expect(pauseResumeInfo).toBeHidden();

    //update schedule
    const selectedDay = "Tuesday"
    let selectWeekday = page.locator('select#weekday').first();
    await selectWeekday.selectOption(selectedDay);
    await updateScheduleBtn.scrollIntoViewIfNeeded();
    await updateScheduleBtn.click();

    statusInfo = await page.locator('div[name="statusInfo"]').innerText();
    expect(statusInfo.includes(`Next run will be on ${selectedDay}`)).toBe(true);

    //add and remove members to monitoring team

    await addMonitoringTeamMemberBtn.scrollIntoViewIfNeeded();
    page.once('dialog', async dialog => {
        expect(dialog.type()).toBe('prompt');
        expect(dialog.message()).toBe(
            'Enter the email address of the new monitoring team member:'
        );
        await dialog.accept(memberEmail);
    });
    await addMonitoringTeamMemberBtn.click();
    await page.waitForTimeout(1000);
    let members = await page.locator('span[data-name="monitoringTeamMember"]');
    let numOfMembers = await members.count();
    expect(numOfMembers).toBeGreaterThan(0);
    const member = await members.filter({ hasText: memberEmail });
    await expect(member).toBeVisible();

    const removeMonitoringTeamMemberBtn = await member.locator('[data-name="remove-monitoring-team-member-btn"]');
    await removeMonitoringTeamMemberBtn.scrollIntoViewIfNeeded();
    page.once('dialog', async dialog => {
        expect(dialog.type()).toBe('confirm');
        expect(dialog.message()).toBe(
            `Are you sure you want to remove ${memberEmail} from the monitoring team?`
        );
        await dialog.accept();
    });
    await removeMonitoringTeamMemberBtn.click();
    await page.waitForTimeout(1000);
    members = await page.locator('span[data-name="monitoringTeamMember"]');
    await expect(members.filter({ hasText: memberEmail })).not.toBeVisible();
});

test ('test quick entry to add and remove users to biosecurity', async ({ page }) => {
    await login(page);
    await page.goto('/biosecurity');
    await page.waitForURL('**/biosecurity');
    await page.waitForTimeout(1000);

    let alertCards = page.locator('div[data-name="alert-card"]');
    await expect(alertCards.first()).toBeVisible();

    let quickEntry = page.locator('div[data-name="quick-entry"]').first();
    await quickEntry.scrollIntoViewIfNeeded();
    let listIdInput = quickEntry.locator('input[name="listid"]').first();
    let userEmails= quickEntry.locator('input[name="useremails"]').first();
    let submitQuickEntryBtn = quickEntry.locator('button#quick-submit').first();
    await listIdInput.fill(listId);
    await userEmails.fill(memberEmail);
    await expect(listIdInput).toHaveValue(listId);
    await expect(userEmails).toHaveValue(memberEmail);
    await submitQuickEntryBtn.click();

    //after adding the user, the alert card should be moved to the top of the list,
    // and the alert name should be displayed in the alert card, and the user email should be displayed in the alert card.
    await page.waitForTimeout(1000);
    let alertDiv = page.locator('div[data-name="alert-card"]').first();
    await expect(alertDiv).toBeVisible();
    let alertNameLink = alertDiv.locator('a[data-name="alert-name"]').first();
    await expect(alertNameLink).toHaveText(alertName);

    let alertEmails = alertDiv.locator('span[data-name="subscriber-email-address"]');
    let numberOfEmails = await alertEmails.count();
    expect(numberOfEmails).toBeGreaterThan(0);
    await expect(alertEmails.filter({ hasText: memberEmail })).toBeVisible();

    const removeSubscriberBtn = alertDiv.locator('[data-name="remove-subscriber"]').first();
    await removeSubscriberBtn.scrollIntoViewIfNeeded();
    await removeSubscriberBtn.click();
    await page.waitForTimeout(1000);
    alertDiv = page.locator('div[data-name="alert-card"]').first();
    await expect(alertCards.first()).toBeVisible();
    alertEmails = alertDiv.locator('span[data-name="subscriber-email-address"]');
    const currentNumberOfEmails = await alertEmails.count();
    expect(currentNumberOfEmails).toEqual(numberOfEmails - 1);
    await expect(alertEmails.filter({ hasText: memberEmail })).not.toBeVisible();

    //delete all subscribers of the alert
    if (currentNumberOfEmails > 0) {
        const removeSubscriberBtns = await alertDiv.locator('[data-name="remove-subscriber"]').all();
        for (const removeSubscriberBtn of removeSubscriberBtns) {
            await removeSubscriberBtn.scrollIntoViewIfNeeded();
            await removeSubscriberBtn.click();
            await page.waitForTimeout(500);
        }
    }
    await page.waitForTimeout(1000);
    const deleteAlertBtn = alertDiv.locator('[name="delete-subscription"]').first();
    await deleteAlertBtn.scrollIntoViewIfNeeded();
    page.once('dialog', async dialog => {
        expect(dialog.type()).toBe('confirm');
        expect(dialog.message()).toBe(
            `Delete "${alertName}"?`
        );
        await dialog.accept();
    });
    await deleteAlertBtn.click();
    await page.waitForTimeout(1000);
    alertNameLink = alertDiv.locator('a[data-name="alert-name"]', { hasText: alertName });
    await expect(alertNameLink).toHaveCount(0);
})

test ('add user to a random alert', async ({ page }) => {
    await login(page);
    await page.goto('/biosecurity');
    await page.waitForURL('**/biosecurity');
    await page.waitForTimeout(1000);

    let alertDivs = page.locator('div[data-name="alert-card"]');
    await expect(alertDivs.first()).toBeVisible();
    const numOfAlerts = await alertDivs.count();
    const randomAlertIndex = Math.floor(Math.random() * numOfAlerts);
    let alertDiv = alertDivs.nth(randomAlertIndex);
    await expect(alertDiv).toBeVisible();

    const subscriberEmails = alertDiv.locator('span[data-name="subscriber-email-address"]');
    const numberOfSubscriberEmails = await subscriberEmails.count();
    page.once('dialog', async dialog => {
        expect(dialog.type()).toBe('prompt');
        expect(dialog.message()).toBe(
            'Multiple user email addresses separated by \';\''
        );
        await dialog.accept(memberEmail);
    });

    await alertDiv.locator('[data-name="add-subscriber"]').click();
    await page.waitForTimeout(1000);
    const currentNumberOfSubscriberEmails = await subscriberEmails.count();
    expect(currentNumberOfSubscriberEmails).toEqual(numberOfSubscriberEmails + 1);
    await expect(subscriberEmails.filter({ hasText: memberEmail })).toBeVisible();

    //remove the user from the alert
    const removeSubscriberBtn = alertDiv.locator('[data-name="remove-subscriber"]').first();
    await removeSubscriberBtn.scrollIntoViewIfNeeded();
    await removeSubscriberBtn.click();
    await page.waitForTimeout(1000);
    const finalNumberOfSubscriberEmails = await subscriberEmails.count();
    expect(finalNumberOfSubscriberEmails).toEqual(numberOfSubscriberEmails);
    await expect(subscriberEmails.filter({ hasText: memberEmail })).not.toBeVisible();
})

test ('test biosecurity csv display', async ({ page }) => {
    await login(page);
    await page.goto('/biosecurity/csv');
    await page.waitForURL('**/biosecurity/csv');
    await page.waitForTimeout(1000);

    const h2 = page.locator('h2').filter({ hasText: 'Biosecurity Alerts Reports' });
    await expect(h2).toBeVisible();
    const stats = page.locator('span[data-name="csv-stats"]');
    await expect(stats).toBeVisible();
    await stats.scrollIntoViewIfNeeded();
    await expect(stats).toContainText(/\d+ files\s*,\s*.+ in total/);

    const foldersFilesDiv = page.locator('div[data-name="csv-folders-files"]');
    await expect(foldersFilesDiv).toBeVisible();
    await foldersFilesDiv.scrollIntoViewIfNeeded();
    const folders = foldersFilesDiv.locator('div.folder');
    const numOfFolders = await folders.count();
    //pickup random one
    const randomFolderIndex = Math.floor(Math.random() * numOfFolders);
    const randomFolder = folders.nth(randomFolderIndex);
    await randomFolder.scrollIntoViewIfNeeded();
    await expect(randomFolder).toBeVisible();

    //<div class="folder" data-folder="2026-09-29">
    const folderName = await randomFolder.innerText();
    const dataFolder = await randomFolder.getAttribute('data-folder');
    expect(folderName.trim()).toBe(dataFolder);

    await randomFolder.scrollIntoViewIfNeeded();
    // <i class="fa fa-folder folder-icon folder" aria-hidden="true"></i>
    const folderIcon = randomFolder.locator('i.fa-folder');
    await expect(folderIcon).toBeVisible();

    // Clicking the folder icon, the class should change to far fa-folder-open
    // vice versa
    let folderIconClass = await folderIcon.getAttribute('class');
    let isFolderOpen = folderIconClass?.includes('far fa-folder-open');
    await folderIcon.scrollIntoViewIfNeeded();
    await folderIcon.click();
    await page.waitForTimeout(1000);

    folderIconClass = await folderIcon.getAttribute('class');
    expect(folderIconClass?.includes('far fa-folder-open')).not.toBe(isFolderOpen);

    //<div class="file-list" id="files-2026-09-29" style="display: block;">
    const fileList = foldersFilesDiv.locator(`div#files-${dataFolder}.file-list`);
    await expect(fileList).toBeVisible();
})