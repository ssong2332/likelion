// src/api/glossary.js
import { request } from './config.js';

export async function getGlossaries() {
  return request('/api/glossaries', { method: 'GET' });
}

export async function addGlossary({ term, rule, note }) {
  return request('/api/glossaries', {
    method: 'POST',
    body: JSON.stringify({ term, rule, note: note ?? null }),
  });
}

export async function updateGlossary(id, { term, rule, note }) {
  return request(`/api/glossaries/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ term, rule, note: note ?? null }),
  });
}

export async function deleteGlossary(id) {
  return request(`/api/glossaries/${id}`, {
    method: 'DELETE',
  });
}