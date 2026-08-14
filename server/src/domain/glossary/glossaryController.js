// In-Memory store for Glossary CRUD (Backend B - F-5)
let glossaries = [
  { id: '1', term: 'Manyfast', rule: '원문 유지 (Keep Original)', note: '의역 금지' },
  { id: '2', term: 'ASAP', rule: '오늘 EOD 18:00 전', note: '팀 내 합의 기준' }
];

export function getGlossaries(req, res) {
  res.json({ data: glossaries });
}

export function createGlossary(req, res) {
  const { term, rule, note } = req.body;
  const newEntry = { id: String(Date.now()), term, rule: rule || '원문 유지', note: note || '' };
  glossaries.push(newEntry);
  res.status(201).json({ data: newEntry });
}

export function deleteGlossary(req, res) {
  const { id } = req.params;
  glossaries = glossaries.filter(g => g.id !== id);
  res.json({ message: 'Glossary entry deleted successfully' });
}
