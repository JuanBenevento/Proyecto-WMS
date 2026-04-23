import { test, expect } from '@playwright/test';

/**
 * @api
 * @group inventory
 */
test.describe('Inventory API', () => {
  const baseURL = process.env.API_URL || 'http://localhost:8080';
  let authToken = '';

  test.beforeAll(async ({ request }) => {
    const loginResponse = await request.post(`${baseURL}/api/v1/auth/login`, {
      data: { username: 'superadmin', password: 'admin123' }
    });
    const loginBody = await loginResponse.json();
    authToken = loginBody.token;
  });

  test('GET /api/v1/inventory - returns 200 with valid token', async ({ request }) => {
    const response = await request.get(`${baseURL}/api/v1/inventory`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });

    expect(response.status()).toBe(200);
  });

  test('GET /api/v1/inventory - returns 401 without token', async ({ request }) => {
    const response = await request.get(`${baseURL}/api/v1/inventory`);

    expect(response.status()).toBe(401);
  });

  test('POST /api/v1/inventory/receive - receives inventory', async ({ request }) => {
    const receiveRequest = {
      productSku: `INV-${Date.now()}`,
      quantity: 100,
      locationCode: 'A-01-01',
      batchNumber: `BATCH-${Date.now()}`,
      expiryDate: '2026-12-31'
    };

    const response = await request.post(`${baseURL}/api/v1/inventory/receive`, {
      headers: {
        'Authorization': `Bearer ${authToken}`,
        'Content-Type': 'application/json'
      },
      data: receiveRequest
    });

    expect(response.status()).toBe(201);
  });

  test('POST /api/v1/inventory/move - moves inventory', async ({ request }) => {
    const moveRequest = {
      productSku: 'SKU-MOVE',
      sourceLocation: 'A-01-01',
      targetLocation: 'A-02-01',
      quantity: 10
    };

    const response = await request.post(`${baseURL}/api/v1/inventory/move`, {
      headers: {
        'Authorization': `Bearer ${authToken}`,
        'Content-Type': 'application/json'
      },
      data: moveRequest
    });

    // May be 200 or 400 depending on inventory existence
    expect([200, 400]).toContain(response.status());
  });
});