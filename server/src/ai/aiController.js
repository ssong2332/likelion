import { processRefinement, processReplyDrafts } from './aiService.js';
import { calculateTimezoneInfo } from '../domain/timezone/timezoneService.js';

export async function handleRefinement(req, res, next) {
  try {
    const { originalText, targetLang, senderTimezone, receiverTimezone, collaborationStyle, appliedGlossaryIds } = req.body;
    
    if (!originalText) {
      return res.status(400).json({ error: { message: 'originalText is required' } });
    }

    const aiResult = await processRefinement({ originalText, targetLang, collaborationStyle });
    const timezoneInfo = calculateTimezoneInfo(senderTimezone, receiverTimezone);

    res.json({
      ...aiResult,
      timezoneInfo
    });
  } catch (err) {
    next(err);
  }
}

export async function handleReplyDrafts(req, res, next) {
  try {
    const { receivedMessage } = req.body;
    if (!receivedMessage) {
      return res.status(400).json({ error: { message: 'receivedMessage is required' } });
    }

    const replyResult = await processReplyDrafts({ receivedMessage });
    res.json(replyResult);
  } catch (err) {
    next(err);
  }
}
