import { test as base, Page, APIRequest } from '@playwright/test';
import dotenv from 'dotenv';

dotenv.config({ path: '.env.example' });

export * from '@playwright/test';

interface WmsFixtures {
  (page: Page): Promise<void>;
}

/**
 * Test fixture for API tests
 */
export const testApi = base.extend({
  request: async ({ baseURL }, use) => {
    // Set JWT token for authenticated requests
    const request = new APIRequest(baseURL!);
    await use(request);
  },
});

/**
 * Authenticated test helper
 */
export async function loginAs(page: Page, credentials: { username: string; password: string }) {
  await page.goto('/login');
  await page.fill('input[formcontrolname="username"]', credentials.username);
  await page.fill('input[formcontrolname="password"]', credentials.password);
  await page.click('button[type="submit"]');
  await page.waitForURL(/\/(dashboard|orders|inventory)/);
}

/**
 * Create test order via API
 */
export async function createTestOrder(api: APIRequest, token: string, order: object) {
  const response = await api.post('/api/v1/orders', {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    data: order,
  });
  return response;
}

/**
 * Get JWT token via API
 */
export async function getAuthToken(api: APIRequest, username: string, password: string): Promise<string> {
  const response = await api.post('/api/v1/auth/login', {
    headers: { 'Content-Type': 'application/json' },
    data: { username, password },
  });

  const body = await response.json();
  return body.token;
}

// Test data factories
export const testData = {
  validProduct: (overrides = {}) => ({
    sku: `TEST-SKU-${Date.now()}`,
    name: 'Test Product',
    description: 'E2E Test Product',
    width: 10,
    height: 20,
    depth: 30,
    weight: 5.5,
    ...overrides,
  }),

  validOrder: (overrides = {}) => ({
    customerId: 'CUST-001',
    customerName: 'Test Customer',
    customerEmail: 'test@customer.com',
    shippingAddress: '123 Test St',
    priority: 'NORMAL',
    warehouseId: 'WH-001',
    lines: [
      {
        productSku: 'SKU-001',
        requestedQuantity: 10,
      },
    ],
    ...overrides,
  }),

  validLocation: (overrides = {}) => ({
    code: `LOC-${Date.now()}`,
    type: 'RACK',
    zone: 'ZONE-A',
    capacity: 100,
    ...overrides,
  }),
};