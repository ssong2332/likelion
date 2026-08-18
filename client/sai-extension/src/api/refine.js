// src/api/refine.js
import { request } from './config.js';

export async function analyzeRefine({
  originalText,
  sourceLang = 'ko',
  targetLang = 'en',
  senderTimezone = 'Asia/Seoul',
  receiverTimezone = 'America/New_York',
  collaborationStyle = { tone: 'polite', directness: 'balanced', detailLevel: 'concise' },
  appliedGlossaryIds = [],
  appliedRuleIds = [],
}) {
  return request('/api/ai/analyze-refine', {
    method: 'POST',
    body: JSON.stringify({
      originalText,
      sourceLang,
      targetLang,
      senderTimezone,
      receiverTimezone,
      collaborationStyle,
      appliedGlossaryIds,
      appliedRuleIds,
    }),
  });
}

export async function generateReplyDraft({
  receivedMessage,
  intent,
  sourceLang = 'ko',
  targetLang = 'en',
  collaborationStyle = { tone: 'polite', directness: 'balanced', detailLevel: 'concise' },
}) {
  return request('/api/ai/reply-draft', {
    method: 'POST',
    body: JSON.stringify({
      receivedMessage,
      intent,
      sourceLang,
      targetLang,
      collaborationStyle,
    }),
  });
}