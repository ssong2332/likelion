// src/api/rules.js

let mockRules = [
  { id: 1, name: '보고서 마감', description: '매주 목요일 17:00 KST까지 초안 공유' },
]

export async function getRules() {
  await new Promise((r) => setTimeout(r, 300))
  return { data: mockRules }
}

export async function addRule({ name, description }) {
  await new Promise((r) => setTimeout(r, 300))
  const newItem = { id: Date.now(), name, description }
  mockRules.push(newItem)
  return { data: newItem }
}

export async function updateRule(id, { name, description }) {
  await new Promise((r) => setTimeout(r, 300))
  mockRules = mockRules.map((r) => (r.id === id ? { ...r, name, description } : r))
  return { data: mockRules.find((r) => r.id === id) }
}

export async function deleteRule(id) {
  await new Promise((r) => setTimeout(r, 300))
  mockRules = mockRules.filter((r) => r.id !== id)
  return { message: 'Rule deleted successfully' }
}