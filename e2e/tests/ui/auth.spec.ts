import { test, expect } from '@playwright/test';

/**
 * @ui
 * @group authentication
 */
test.describe('Authentication UI', () => {
  const frontendURL = process.env.FRONTEND_URL || 'http://localhost:4200';

  test('Login page loads correctly', async ({ page }) => {
    await page.goto(`${frontendURL}/login`);
    await expect(page).toHaveTitle(/WMS/i);
    await expect(page.locator('input[formcontrolname="username"]')).toBeVisible();
    await expect(page.locator('input[formcontrolname="password"]')).toBeVisible();
  });

  test('Login with valid credentials redirects to dashboard', async ({ page }) => {
    await page.goto(`${frontendURL}/login`);
    await page.fill('input[formcontrolname="username"]', 'superadmin');
    await page.fill('input[formcontrolname="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/\/(dashboard|orders|inventory)/, { timeout: 10000 });
  });

  test('Login with invalid credentials shows error', async ({ page }) => {
    await page.goto(`${frontendURL}/login`);
    await page.fill('input[formcontrolname="username"]', 'superadmin');
    await page.fill('input[formcontrolname="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');
    await expect(page.locator('.error, .alert, text=Invalid')).toBeVisible({ timeout: 5000 });
  });
});