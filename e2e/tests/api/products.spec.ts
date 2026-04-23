import { test, expect } from '@playwright/test';

/**
 * @api
 * @group products
 */
test.describe('Products API', () => {
  const baseURL = process.env.API_URL || 'http://localhost:8080';
  let authToken = '';

  test.beforeAll(async ({ request }) => {
    // Login and get token
    const loginResponse = await request.post(`${baseURL}/api/v1/auth/login`, {
      data: { username: 'superadmin', password: 'admin123' }
    });
    const loginBody = await loginResponse.json();
    authToken = loginBody.token;
  });

  test('GET /api/v1/products - returns 200 with valid token', async ({ request }) => {
    const response = await request.get(`${baseURL}/api/v1/products`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  test('GET /api/v1/products - returns 401 without token', async ({ request }) => {
    const response = await request.get(`${baseURL}/api/v1/products`);

    expect(response.status()).toBe(401);
  });

  test('POST /api/v1/products - creates product with valid token', async ({ request }) => {
    const product = {
      sku: `TEST-PROD-${Date.now()}`,
      name: 'E2E Test Product',
      description: 'Created by E2E test',
      width: 10,
      height: 20,
      depth: 30,
      weight: 5.5
    };

    const response = await request.post(`${baseURL}/api/v1/products`, {
      headers: {
        'Authorization': `Bearer ${authToken}`,
        'Content-Type': 'application/json'
      },
      data: product
    });

    expect(response.status()).toBe(201);
    const body = await response.json();
    expect(body.sku).toBe(product.sku);
  });

  test('DELETE /api/v1/products/{sku} - deletes product', async ({ request }) => {
    const sku = `TEST-PROD-${Date.now()}`;

    // Create first
    await request.post(`${baseURL}/api/v1/products`, {
      headers: {
        'Authorization': `Bearer ${authToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        sku,
        name: 'To Delete',
        description: 'Test',
        width: 1,
        height: 1,
        depth: 1,
        weight: 1
      }
    });

    // Delete
    const deleteResponse = await request.delete(`${baseURL}/api/v1/products/${sku}`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });

    expect(deleteResponse.status()).toBe(204);
  });
});