import { test, expect } from '@playwright/test';

/**
 * @api
 * @group orders
 */
test.describe('Orders API', () => {
  const baseURL = process.env.API_URL || 'http://localhost:8080';
  let authToken = '';

  test.beforeAll(async ({ request }) => {
    const loginResponse = await request.post(`${baseURL}/api/v1/auth/login`, {
      data: { username: 'superadmin', password: 'admin123' }
    });
    const loginBody = await loginResponse.json();
    authToken = loginBody.token;
  });

  test('GET /api/v1/orders - returns 200 with valid token', async ({ request }) => {
    const response = await request.get(`${baseURL}/api/v1/orders`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body).toHaveProperty('data');
    expect(Array.isArray(body.data)).toBe(true);
  });

  test('POST /api/v1/orders - creates order with valid token', async ({ request }) => {
    const order = {
      customerId: `CUST-E2E-${Date.now()}`,
      customerName: 'E2E Test Customer',
      customerEmail: 'test@e2e.com',
      shippingAddress: '123 E2E Test St',
      priority: 'NORMAL',
      warehouseId: 'WH-001',
      lines: [
        {
          productSku: 'SKU-001',
          requestedQuantity: 5
        }
      ]
    };

    const response = await request.post(`${baseURL}/api/v1/orders`, {
      headers: {
        'Authorization': `Bearer ${authToken}`,
        'Content-Type': 'application/json'
      },
      data: order
    });

    expect(response.status()).toBe(201);
    const body = await response.json();
    expect(body).toHaveProperty('orderId');
    expect(body.customerId).toBe(order.customerId);
  });

  test('POST /api/v1/orders/{id}/confirm - confirms order', async ({ request }) => {
    // Create order first
    const createResponse = await request.post(`${baseURL}/api/v1/orders`, {
      headers: {
        'Authorization': `Bearer ${authToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        customerId: 'CUST-CONFIRM',
        customerName: 'Confirm Test',
        customerEmail: 'confirm@test.com',
        shippingAddress: '123 Test',
        priority: 'NORMAL',
        warehouseId: 'WH-001',
        lines: [{ productSku: 'SKU-001', requestedQuantity: 1 }]
      }
    });

    const order = await createResponse.json();
    const orderId = order.orderId;

    // Confirm
    const confirmResponse = await request.post(`${baseURL}/api/v1/orders/${orderId}/confirm`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });

    expect(confirmResponse.status()).toBe(200);
    const confirmed = await confirmResponse.json();
    expect(confirmed.status).toBe('CONFIRMED');
  });

  test('POST /api/v1/orders/{id}/cancel - cancels order', async ({ request }) => {
    // Create order
    const createResponse = await request.post(`${baseURL}/api/v1/orders`, {
      headers: {
        'Authorization': `Bearer ${authToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        customerId: 'CUST-CANCEL',
        customerName: 'Cancel Test',
        customerEmail: 'cancel@test.com',
        shippingAddress: '123 Test',
        priority: 'NORMAL',
        warehouseId: 'WH-001',
        lines: [{ productSku: 'SKU-001', requestedQuantity: 1 }]
      }
    });

    const order = await createResponse.json();
    const orderId = order.orderId;

    // Cancel
    const cancelResponse = await request.post(`${baseURL}/api/v1/orders/${orderId}/cancel`, {
      headers: {
        'Authorization': `Bearer ${authToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        cancellationReason: 'CUSTOMER_CANCELLED',
        cancelledBy: 'superadmin'
      }
    });

    expect(cancelResponse.status()).toBe(200);
    const cancelled = await cancelResponse.json();
    expect(cancelled.status).toBe('CANCELLED');
  });
});