// API Client for Manyfast Frontend

export async function analyzeAndRefine(payload) {
  try {
    const response = await fetch('/api/ai/analyze-refine', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!response.ok) throw new Error('API request failed');
    return await response.json();
  } catch (err) {
    console.warn('Backend API unavailable, using local mock fallback:', err);
    // Mock fallback response matching API_CONTRACT.md
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
      ],
      timezoneInfo: {
        senderLocalTime: new Date().toISOString(),
        receiverLocalTime: new Date(Date.now() - 13 * 3600 * 1000).toISOString(),
        isReceiverOffHours: true,
        nextAvailableCheckingTime: "현지 시각 오전 09:00 EST (약 6시간 뒤)"
      }
    };
  }
}

export async function generateReplyDrafts(payload) {
  try {
    const response = await fetch('/api/ai/reply-draft', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!response.ok) throw new Error('API request failed');
    return await response.json();
  } catch (err) {
    console.warn('Backend API unavailable, using local mock fallback:', err);
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
}
