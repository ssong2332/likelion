// In-Memory store for Message History & Deletion (Backend B - F-7)
let messageHistory = [];

export function getHistory(req, res) {
  res.json({
    totalCount: messageHistory.length,
    history: messageHistory
  });
}

export function deleteHistoryItem(req, res) {
  const { id } = req.params;
  messageHistory = messageHistory.filter(m => m.id !== id);
  res.json({ message: 'History item permanently deleted' });
}

export function deleteAllHistory(req, res) {
  messageHistory = [];
  res.json({ message: 'All message history permanently deleted' });
}
