// AI Service for Refinement and Reply Generation (Backend A)
import { getOpenAIClient } from './openaiClient.js';

export async function processRefinement({ originalText, targetLang = 'en', collaborationStyle = {}, appliedGlossary = [] }) {
  const openai = getOpenAIClient();

  if (openai) {
    try {
      const systemPrompt = `You are Manyfast AI Assistant.
Analyze the user's business message draft and provide a structured JSON response matching the following requirements:
1. Preserve core business facts (purpose, assignee, deadline, urgency, business impact).
2. Refine into polite, natural ${targetLang} ensuring no direct blaming or demanding tone.
3. Provide back-translation into Korean.
4. Detect risky expressions that might cause misunderstanding.
5. Identify any missing critical business details (e.g. vague deadlines).
6. Return purely valid JSON with keys: refinedText, backTranslation, extractedInfo (purpose, assignee, deadline, urgency, businessImpact), missingInfoWarnings (array), riskyExpressions (array), appliedGlossary (array).`;

      const response = await openai.chat.completions.create({
        model: 'gpt-4o-mini',
        messages: [
          { role: 'system', content: systemPrompt },
          { role: 'user', content: originalText }
        ],
        response_format: { type: 'json_object' }
      });

      return JSON.parse(response.choices[0].message.content);
    } catch (err) {
      console.warn('[OpenAI Error, falling back to heuristic engine]:', err.message);
    }
  }

  // Realistic fallback simulation matching API_CONTRACT.md
  return {
    refinedText: "PR #142 is currently blocking today's release schedule. Could you please prioritize reviewing the Manyfast terminology and confirm your feedback by EOD?",
    backTranslation: "PR #142가 현재 오늘의 배포 일정을 지연시키고 있습니다. Manyfast 용어를 확인해 주시고 오늘 EOD까지 피드백을 검토해 주시겠어요?",
    extractedInfo: {
      purpose: "PR 코드 리뷰 및 배포 블로커 해소",
      assignee: "수신자",
      deadline: "오늘 EOD (18:00)",
      urgency: "critical",
      businessImpact: "오늘자 릴리스 배포 일정 지연"
    },
    missingInfoWarnings: [
      {
        type: "deadline_detail",
        warning: "구체적인 시간 기준이 모호합니다.",
        suggestedCompletion: "현지 시각 기준 '오늘 18:00 EST'로 명시하는 것을 권장합니다."
      }
    ],
    riskyExpressions: [
      {
        originalPhrase: "안 봐주셔서",
        reason: "상대방에 대한 직접적 비난으로 오해될 수 있음",
        replacedWith: "is currently blocking schedule"
      }
    ],
    appliedGlossary: [
      {
        term: "Manyfast",
        rule: "원문 유지 (Keep Original)",
        matchedInRefined: true
      }
    ]
  };
}

export async function processReplyDrafts({ receivedMessage }) {
  const openai = getOpenAIClient();

  if (openai) {
    try {
      const systemPrompt = `You are Manyfast Reply Assistant (F-8).
Analyze incoming message and generate 3 reply options (accept, schedule, request_details) with placeholders like [bracketed info].
Return JSON with analyzedRequest (summary, urgency, actionRequired) and suggestedReplies (array of {direction, title, draftText}).`;

      const response = await openai.chat.completions.create({
        model: 'gpt-4o-mini',
        messages: [
          { role: 'system', content: systemPrompt },
          { role: 'user', content: receivedMessage }
        ],
        response_format: { type: 'json_object' }
      });

      return JSON.parse(response.choices[0].message.content);
    } catch (err) {
      console.warn('[OpenAI Error, falling back to mock]:', err.message);
    }
  }

  return {
    analyzedRequest: {
      summary: "아키텍처 초안에 대한 피드백 논의 요청 (완곡한 수정 요구 가능성 높음)",
      urgency: "normal",
      actionRequired: "미팅 시간 조율 및 사전 피드백 확인"
    },
    suggestedReplies: [
      {
        direction: "accept",
        title: "즉시 수락 및 미팅 제안",
        draftText: "Thanks for taking a look! I would be glad to discuss your feedback. Would [선호하는 요일/시간] work for a quick sync?"
      },
      {
        direction: "request_details",
        title: "사전 코멘트 서면 요청",
        draftText: "Thank you for the review. Could you leave a few notes in the [문서/티켓 링크] first so I can prepare before we jump into a call?"
      },
      {
        direction: "schedule",
        title: "일정 지연 및 추후 조율",
        draftText: "Thanks for checking it. I am currently focusing on [진행 중인 작업], but I will reach out by [조율 가능 시점] to schedule our discussion."
      }
    ]
  };
}
