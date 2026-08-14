// In-Memory store for Rules CRUD (Backend B - F-5)
let rules = [
  { id: '1', name: '보고서 마감', description: '매주 목요일 17:00 KST까지 초안 공유' }
];

export function getRules(req, res) {
  res.json({ data: rules });
}

export function createRule(req, res) {
  const { name, description } = req.body;
  const newRule = { id: String(Date.now()), name, description };
  rules.push(newRule);
  res.status(201).json({ data: newRule });
}

export function deleteRule(req, res) {
  const { id } = req.params;
  rules = rules.filter(r => r.id !== id);
  res.json({ message: 'Rule deleted successfully' });
}
