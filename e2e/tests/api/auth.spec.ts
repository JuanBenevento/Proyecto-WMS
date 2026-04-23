import { test, expect } from '@playwright/test';

/**
 * @api
 * @group authentication
 */
test.describe('Authentication API', () => {
  const baseURL = process.env.API_URL || 'http://localhost:8080';

  test('POST /api/v1/auth/login - returns 200 with valid credentials', async ({ request }) => {
    const response = await request.post(`${baseURL}/api/v1/auth/login`, {
      data: {
        username: 'superadmin',
        password: 'admin123',
      },
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body).toHaveProperty('token');
    expect(body).toHaveProperty('username', 'superadmin');
    expect(body).toHaveProperty('role');
  });

  test('POST /api/v1/auth/login - returns 401 with invalid credentials', async ({ request }) => {
    const response = await request.post(`${baseURL}/api/v1/auth/login`, {
      data: {
        username: 'superadmin',
        password: 'wrong-password',
      },
    });

    expect(response.status()).toBe(401);
  });

  test('POST /api/v1/auth/login - returns 400 with missing credentials', async ({ request }) => {
    const response = await request.post(`${baseURL}/api/v1/auth/login`, {
      data: {},
    });

    expect(response.status()).toBe(400);
  });
});