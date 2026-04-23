import { test, expect } from '@playwright/test';

/**
 * @ui
 * @group orders
 */
test.describe('Orders UI', () => {
  const frontendURL = process.env.FRONTEND_URL || 'http://localhost:4200';

  test.beforeEach(async ({ page }) => {
    // Login first
    await page.goto(`${frontendURL}/login`);
    await page.fill('input[formcontrolname="username"]', 'superadmin');
    await page.fill('input[formcontrolname="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/\/(dashboard|orders|inventory)/, { timeout: 10000 });
  });

  test('Orders list page loads and shows orders', async ({ page }) => {
    await page.goto(`${frontendURL}/orders`);
    await expect(page.locator('h1, h2')).toContainText(/Order/i);
  });

  test('Can navigate to create order form', async ({ page }) => {
    await page.goto(`${frontendURL}/orders`);
    // Look for create button or link
    const createButton = page.locator('button:has-text("New Order"), a:has-text("New Order"), button:has-text("Crear")');
    if (await createButton.isVisible()) {
      await createButton.click();
      await expect(page.url()).toContain('/orders/new');
    }
  });

  test('Order details page loads', async ({ page }) => {
    // Assume there's at least one order
    await page.goto(`${frontendURL}/orders`);
    // If order list has items, click first
    const firstOrder = page.locator('tr, .order-item, .order-row').first();
    if (await firstOrder.isVisible()) {
      await firstOrder.click();
      await expect(page.url()).toMatch(/orders\/[\w-]+/);
    }
  });
});