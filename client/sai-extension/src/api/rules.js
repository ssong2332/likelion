// src/api/rules.js
import { request } from './config.js';

export async function getRules() {
  return request('/api/rules', { method: 'GET' });
}

export async function addRule({ name, description }) {
  return request('/api/rules', {
    method: 'POST',
    body: JSON.stringify({ name, description: description ?? '' }),
  });
}

export async function updateRule(id, { name, description }) {
  return request(`/api/rules/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ name, description: description ?? '' }),
  });
}

export async function deleteRule(id) {
  return request(`/api/rules/${id}`, {
    method: 'DELETE',
  });
}