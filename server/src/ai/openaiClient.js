import OpenAI from 'openai';
import { config } from '../config/index.js';

let openaiInstance = null;

export function getOpenAIClient() {
  if (!openaiInstance && config.openaiApiKey) {
    openaiInstance = new OpenAI({ apiKey: config.openaiApiKey });
  }
  return openaiInstance;
}
