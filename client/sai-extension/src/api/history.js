// src/api/history.js
import { request } from './config.js';

export async function getMessageHistory() {
  return request('/api/messages/history', { method: 'GET' });
}

export async function deleteMessageHistoryItem(id) {
  return request(`/api/messages/${id}`, { method: 'DELETE' });
}

export async function deleteAllMessageHistory() {
  return request('/api/messages/all', { method: 'DELETE' });
}
