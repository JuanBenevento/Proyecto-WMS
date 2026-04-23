import { test, expect } from '@playwright/test';

/**
 * @ui
 * @group inventory
 */
test.describe('Inventory UI', () => {
  const frontendURL = process.env.FRONTEND_URL || 'http://localhost:4200';

  test.beforeEach(async ({ page }) => {
    await page.goto(`${frontendURL}/login`);
    await page.fill('input[formcontrolname="username"]', 'superadmin');
    await page.fill('input[formcontrolname="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/\/(dashboard|orders|inventory)/, { timeout: 10000 });
  });

  test('Inventory list page loads', async ({ page }) => {
    await page.goto(`${frontendURL}/inventory`);
    await expect(page.locator('h1, h2')).toContainText(/Inventory/i);
  });

  test('Can navigate to receive page', async ({ page }) => {
    await page.goto(`${frontendURL}/inventory/receive`);
    await expect(page.locator('form, input, button')).toBeVisible();
  });

  test('Can navigate to move page', async ({ page }) => {
    await page.goto(`${frontendURL}/inventory/move`);
    await expect(page.locator('form, input, button')).toBeVisible();
  });
});