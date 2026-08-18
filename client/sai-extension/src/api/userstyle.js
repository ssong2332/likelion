// src/api/userstyle.js
import { request } from './config.js';

export async function getUserStyle() {
  return request('/api/user/collaboration-style', { method: 'GET' });
}

export async function updateUserStyle({ tone, directness, detailLevel }) {
  return request('/api/user/collaboration-style', {
    method: 'PUT',
    body: JSON.stringify({ tone, directness, detailLevel }),
  });
}
