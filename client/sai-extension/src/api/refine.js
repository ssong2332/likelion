// src/api/refine.js

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
  await new Promise((resolve) => setTimeout(resolve, 1000))

  // 실제로는 fetch('/api/ai/analyze-refine', { method: 'POST', body: ... }) 로 교체될 부분
  return {
    refinedText: `${originalText} (교정된 버전 - ${targetLang})`,
    backTranslation: `${originalText} (역번역 예시)`,
    extractedInfo: {
      purpose: '예시 목적',
      assignee: '수신자',
      deadline: '오늘 EOD',
      urgency: 'critical',
      businessImpact: '예시 영향 설명',
    },
    missingInfoWarnings: [
      {
        type: 'deadline_detail',
        warning: '구체적인 시간 기준이 모호합니다.',
        suggestedCompletion: '현지 시각 기준으로 명시하는 것을 권장합니다.',
      },
    ],
    riskyExpressions: [
      {
        originalPhrase: '예시 원문 표현',
        reason: '오해될 수 있는 표현',
        replacedWith: '순화된 표현',
      },
    ],
    appliedGlossary: [],
    timezoneInfo: {
      senderLocalTime: new Date().toISOString(),
      receiverLocalTime: new Date().toISOString(),
      isReceiverOffHours: true,
      nextAvailableCheckingTime: null,
    },
  }
}