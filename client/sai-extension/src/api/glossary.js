// src/api/glossary.js

let mockGlossaries = [
  { id: 1, term: 'EOD', rule: 'End of Day', note: '업무 종료 전까지' },
]

export async function getGlossaries() {
  await new Promise((r) => setTimeout(r, 300))
  return { data: mockGlossaries }
}

export async function addGlossary({ term, rule, note }) {
  await new Promise((r) => setTimeout(r, 300))
  const newItem = { id: Date.now(), term, rule, note: note ?? null }
  mockGlossaries.push(newItem)
  return { data: newItem }
}

export async function updateGlossary(id, { term, rule, note }) {
  await new Promise((r) => setTimeout(r, 300))
  mockGlossaries = mockGlossaries.map((g) =>
    g.id === id ? { ...g, term, rule, note: note ?? null } : g
  )
  return { data: mockGlossaries.find((g) => g.id === id) }
}

export async function deleteGlossary(id) {
  await new Promise((r) => setTimeout(r, 300))
  mockGlossaries = mockGlossaries.filter((g) => g.id !== id)
  return { message: 'Glossary entry deleted successfully' }
}