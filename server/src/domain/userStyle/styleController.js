// In-Memory store for User Style (Backend B - F-6)
let userStyle = {
  tone: 'polite',
  directness: 'balanced',
  detailLevel: 'concise'
};

export function getUserStyle(req, res) {
  res.json({ data: userStyle });
}

export function updateUserStyle(req, res) {
  userStyle = { ...userStyle, ...req.body };
  res.json({ data: userStyle });
}
