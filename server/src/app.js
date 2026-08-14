import express from 'express';
import cors from 'cors';
import { config } from './config/index.js';
import { errorHandler } from './middlewares/errorHandler.js';

// Controllers
import { handleRefinement, handleReplyDrafts } from './ai/aiController.js';
import { handleConvertTimezone } from './domain/timezone/timezoneController.js';
import { getGlossaries, createGlossary, deleteGlossary } from './domain/glossary/glossaryController.js';
import { getRules, createRule, deleteRule } from './domain/rules/rulesController.js';
import { getUserStyle, updateUserStyle } from './domain/userStyle/styleController.js';
import { getHistory, deleteHistoryItem, deleteAllHistory } from './domain/history/historyController.js';

const app = express();

// Middlewares
app.use(cors({ origin: config.clientOrigin, credentials: true }));
app.use(express.json());

// Health Check
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', service: 'Manyfast API Server' });
});

// 🧠 Backend A: AI Engine Routes
app.post('/api/ai/analyze-refine', handleRefinement);
app.post('/api/ai/reply-draft', handleReplyDrafts);

// ⚙️ Backend B: Domain & Timezone & CRUD Routes
app.post('/api/timezone/convert', handleConvertTimezone);

// Glossary & Rules (F-5)
app.get('/api/glossaries', getGlossaries);
app.post('/api/glossaries', createGlossary);
app.delete('/api/glossaries/:id', deleteGlossary);

app.get('/api/rules', getRules);
app.post('/api/rules', createRule);
app.delete('/api/rules/:id', deleteRule);

// User Style (F-6)
app.get('/api/user/collaboration-style', getUserStyle);
app.put('/api/user/collaboration-style', updateUserStyle);

// Message History & Privacy (F-7)
app.get('/api/messages/history', getHistory);
app.delete('/api/messages/all', deleteAllHistory);
app.delete('/api/messages/:id', deleteHistoryItem);

// Global Error Handler
app.use(errorHandler);

if (process.env.NODE_ENV !== 'test') {
  app.listen(config.port, () => {
    console.log(`🚀 Manyfast API Server running on port ${config.port}`);
  });
}

export default app;
