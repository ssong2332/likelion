# 🔌 매니패스트 핵심 API 입출력 명세서 (API Contract & Mock Data)

프론트엔드와 백엔드가 개발 첫날부터 병렬 개발(Mocking)을 진행할 수 있도록 사전 정의된 핵심 API 스키마입니다.

---

## 1. `POST /api/ai/analyze-refine` (AI 교정 및 분석)

### Request
```json
{
  "originalText": "이거 리뷰 3일째 안 봐주셔서 오늘 배포 못 나갑니다. Manyfast 용어 확인해서 오늘 안에 피드백 주세요.",
  "sourceLang": "ko",
  "targetLang": "en",
  "senderTimezone": "Asia/Seoul",
  "receiverTimezone": "America/New_York",
  "collaborationStyle": {
    "tone": "polite",
    "directness": "balanced",
    "detailLevel": "concise"
  },
  "appliedGlossaryIds": ["glossary_1"],
  "appliedRuleIds": ["rule_1"]
}
```

### Response (200 OK)
```json
{
  "refinedText": "PR #142 is currently blocking today's release schedule. Could you please prioritize reviewing the Manyfast terminology and confirm your feedback by EOD?",
  "backTranslation": "PR #142가 현재 오늘의 배포 일정을 지연시키고 있습니다. Manyfast 용어를 확인해 주시고 오늘 EOD까지 피드백을 검토해 주시겠어요?",
  "extractedInfo": {
    "purpose": "PR 코드 리뷰 및 배포 블로커 해소",
    "assignee": "수신자",
    "deadline": "오늘 EOD (18:00)",
    "urgency": "critical",
    "businessImpact": "오늘자 릴리스 배포 일정 지연"
  },
  "missingInfoWarnings": [
    {
      "type": "deadline_detail",
      "warning": "구체적인 시간 기준이 모호합니다.",
      "suggestedCompletion": "현지 시각 기준 '오늘 18:00 EST'로 명시하는 것을 권장합니다."
    }
  ],
  "riskyExpressions": [
    {
      "originalPhrase": "안 봐주셔서",
      "reason": "상대방에 대한 직접적 비난으로 오해될 수 있음",
      "replacedWith": "is currently blocking schedule"
    }
  ],
  "appliedGlossary": [
    {
      "term": "Manyfast",
      "rule": "원문 유지 (Keep Original)",
      "matchedInRefined": true
    }
  ],
  "timezoneInfo": {
    "senderLocalTime": "2026-08-14T16:00:00+09:00",
    "receiverLocalTime": "2026-08-14T03:00:00-04:00",
    "isReceiverOffHours": true,
    "nextAvailableCheckingTime": "2026-08-14T09:00:00-04:00"
  }
}
```

---

## 2. `POST /api/ai/reply-draft` (수신 메시지 기반 회신 초안 생성 - F-8)

### Request
```json
{
  "receivedMessage": "I have a few minor comments on your architecture draft. When you get a chance, let's discuss.",
  "receiverTimezone": "America/New_York",
  "replyDirection": "accept | schedule | request_details"
}
```

### Response (200 OK)
```json
{
  "analyzedRequest": {
    "summary": "아키텍처 초안에 대한 피드백 논의 요청 (완곡한 수정 요구 가능성 높음)",
    "urgency": "normal",
    "actionRequired": "미팅 시간 조율 및 사전 피드백 확인"
  },
  "suggestedReplies": [
    {
      "direction": "accept",
      "title": "즉시 수락 및 미팅 제안",
      "draftText": "Thanks for taking a look! I would be glad to discuss your feedback. Would [선호하는 요일/시간] work for a quick sync?"
    },
    {
      "direction": "request_details",
      "title": "사전 코멘트 서면 요청",
      "draftText": "Thank you for the review. Could you leave a few notes in the [문서/티켓 링크] first so I can prepare before we jump into a call?"
    },
    {
      "direction": "schedule",
      "title": "일정 지연 및 추후 조율",
      "draftText": "Thanks for checking it. I am currently focusing on [진행 중인 작업], but I will reach out by [조율 가능 시점] to schedule our discussion."
    }
  ]
}
```

---

## 3. `POST /api/timezone/convert` (기준 시각 타임존 변환 - F-1)

입력 시각은 ISO-8601 UTC `Instant` 형식으로 전달하고, timezone은 IANA ZoneId를 사용합니다.

### Request
```json
{
  "dateTime": "2026-07-15T12:00:00Z",
  "senderTimezone": "Asia/Seoul",
  "receiverTimezone": "America/New_York"
}
```

### Response (200 OK)
```json
{
  "dateTime": "2026-07-15T12:00:00Z",
  "senderTimezone": "Asia/Seoul",
  "senderLocalTime": "2026-07-15T21:00:00+09:00",
  "receiverTimezone": "America/New_York",
  "receiverLocalTime": "2026-07-15T08:00:00-04:00"
}
```

---

## 4. `POST /api/timezone/check-offhours` (수신자 비업무 시간 판정 - F-1)

기본 업무시간은 수신자 현지 시각 기준 평일 `09:00 <= local time < 18:00`입니다. 주말과 업무시간 전후에는 다음 실제 평일 09:00를 반환합니다.

### Request
```json
{
  "dateTime": "2026-08-22T00:00:00Z",
  "receiverTimezone": "America/New_York"
}
```

### Response (200 OK) — off-hours
```json
{
  "receiverTimezone": "America/New_York",
  "receiverLocalTime": "2026-08-21T20:00:00-04:00",
  "isReceiverOffHours": true,
  "nextAvailableCheckingTime": "2026-08-24T09:00:00-04:00"
}
```

### Response (200 OK) — 업무시간 내
```json
{
  "receiverTimezone": "America/New_York",
  "receiverLocalTime": "2026-08-17T10:00:00-04:00",
  "isReceiverOffHours": false,
  "nextAvailableCheckingTime": null
}
```

### Error (400 Bad Request)
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Invalid IANA timezone: Invalid/Zone"
}
```

---

## 5. Glossary CRUD (용어집 관리 - F-5)

Glossary ID는 MySQL의 `BIGINT` PK를 사용하며 JSON number로 전달합니다. `term`은 전역 Glossary에서 중복될 수 없습니다.

### `GET /api/glossaries`

#### Response (200 OK)
```json
{
  "data": [
    {
      "id": 1,
      "term": "EOD",
      "rule": "End of Day",
      "note": "업무 종료 전까지"
    }
  ]
}
```

빈 Glossary는 다음과 같이 반환합니다.

```json
{
  "data": []
}
```

### `POST /api/glossaries`

#### Request
```json
{
  "term": "EOD",
  "rule": "End of Day",
  "note": "업무 종료 전까지"
}
```

#### Response (201 Created)
```json
{
  "data": {
    "id": 1,
    "term": "EOD",
    "rule": "End of Day",
    "note": "업무 종료 전까지"
  }
}
```

### `PUT /api/glossaries/{id}`

#### Request
```json
{
  "term": "EOD",
  "rule": "End of business day",
  "note": null
}
```

#### Response (200 OK)
```json
{
  "data": {
    "id": 1,
    "term": "EOD",
    "rule": "End of business day",
    "note": null
  }
}
```

### `DELETE /api/glossaries/{id}`

#### Response (200 OK)
```json
{
  "message": "Glossary entry deleted successfully"
}
```

### Error (400 Bad Request) — validation
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "term is required"
}
```

### Error (404 Not Found)
```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Glossary not found: 999"
}
```

### Error (409 Conflict) — duplicate term
```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Glossary term already exists: EOD"
}
```

---

## 6. Rule CRUD (규칙 관리 - F-5)

Rule ID는 MySQL의 `BIGINT` PK를 사용하며 JSON number로 전달합니다. `name`은 전역 Rule에서 중복될 수 없습니다.

### `GET /api/rules`

#### Response (200 OK)
```json
{
  "data": [
    {
      "id": 1,
      "name": "보고서 마감",
      "description": "매주 목요일 17:00 KST까지 초안 공유"
    }
  ]
}
```

빈 Rule 목록은 다음과 같이 반환합니다.

```json
{
  "data": []
}
```

### `POST /api/rules`

#### Request
```json
{
  "name": "보고서 마감",
  "description": "매주 목요일 17:00 KST까지 초안 공유"
}
```

#### Response (201 Created)
```json
{
  "data": {
    "id": 1,
    "name": "보고서 마감",
    "description": "매주 목요일 17:00 KST까지 초안 공유"
  }
}
```

### `PUT /api/rules/{id}`

#### Request
```json
{
  "name": "보고서 마감",
  "description": "매주 금요일 12:00 KST까지 최종본 공유"
}
```

#### Response (200 OK)
```json
{
  "data": {
    "id": 1,
    "name": "보고서 마감",
    "description": "매주 금요일 12:00 KST까지 최종본 공유"
  }
}
```

### `DELETE /api/rules/{id}`

#### Response (200 OK)
```json
{
  "message": "Rule deleted successfully"
}
```

### Error (400 Bad Request) — validation
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "name is required"
}
```

### Error (404 Not Found)
```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Rule not found: 999"
}
```

### Error (409 Conflict) — duplicate name
```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Rule name already exists: 보고서 마감"
}
```
