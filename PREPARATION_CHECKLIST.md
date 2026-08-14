# 📋 해커톤 킥오프 전 필수 사전 준비 체크리스트 (Preparation Checklist)

성공적인 7일 해커톤 개발 착수를 위해 **본격적인 코딩 전 반드시 합의하고 세팅해야 할 5가지 핵심 준비 항목**입니다.

---

## 1. 🔌 FE-BE 병렬 개발을 위한 API 규격(Mock Data) 확정
> 프론트엔드가 백엔드 완성을 기다리지 않고 첫날부터 UI를 구축하려면 입출력 JSON 스키마가 사전에 고정되어야 합니다.

* [ ] **`POST /api/ai/analyze-refine` (핵심 교정 API)** 요청/응답 Mock JSON 확정
* [ ] **`POST /api/ai/reply-draft` (회신 생성 API)** 요청/응답 Mock JSON 확정
* [ ] **`GET/POST /api/glossaries`, `/api/rules` (용어집/규칙 CRUD)** 데이터 모델 확정

---

## 2. 🔑 외부 서비스 계정 및 환경변수(`.env`) 사전 발급
* [ ] **OpenAI API Key**:
  * 모델 선정: `gpt-4o-mini` (응답 속도 1~2초, 가성비 최고) 또는 `gpt-4o`
  * 사용량 한도(Usage Limit) 및 크레딧 사전 확인
* [ ] **클라우드 DB 및 호스팅 인스턴스**:
  * DB: Supabase / PostgreSQL (Neon or Supabase) / Firebase Firestore 중 1개 생성
  * 호스팅: Vercel (프론트엔드), Render 또는 Fly.io (백엔드)
* [ ] **`.env.example` 템플릿 작성**:
  ```env
  PORT=5000
  OPENAI_API_KEY=sk-...
  DATABASE_URL=postgresql://...
  CLIENT_ORIGIN=http://localhost:5173
  ```

---

## 3. 🎭 심사위원 맞춤형 '킬러 데모 시나리오' 2종 선정
> 해커톤 평가는 완성도 높은 "1개의 완벽한 시나리오"를 매끄럽게 보여주는 것이 가장 중요합니다.

* [ ] **시나리오 A (발신 모드: 일정 지연 및 지원 요청)**
  * **원문 (KST 한국어)**: `"이거 리뷰 3일째 안 봐주셔서 오늘 배포 못 나갑니다. Manyfast 용어 확인해서 오늘 안에 피드백 주세요."`
  * **AI 감지 포인트**: 감정 섞인 비난 감지 ➔ [문제/영향/요청] 정제 ➔ 미국 현지 시간(EST 야간) 감지 및 비업무 시간 경고 ➔ `Manyfast` [원문 유지] 용어 적용 ➔ 완곡하면서도 명확한 마감 기한 보존 교정안 도출.
* [ ] **시나리오 B (수신 모드: 회신 초안 생성)**
  * **수신문 (영어 완곡 표현)**: `"I have a few minor comments on your architecture draft. When you get a chance, let's discuss."`
  * **AI 감지 포인트**: 실제 의도(전면 수정 필요 가능성) 파악 ➔ 수락/일정조율/자료요청 3가지 방향의 회신 초안 자동 생성 (플레이스홀더 포함).

---

## 4. 🎨 디자인 에셋 및 공통 라이브러리 선정
* [ ] **컬러 팔레트 정의**:
  * Base: Background (`#F9FAFB`), Text (`#1F2937`), Border (`#E5E7EB`)
  * Accent: Primary Orange (`#FF6B00`), Success Green (`#10B981`), Warning Yellow (`#F59E0B`)
* [ ] **아이콘 라이브러리**: `lucide-react` (통일성 및 경량화)
* [ ] **폰트 설정**: `Pretendard` (한국어/영어 모두 가독성 최적화)

---

## 5. 🗣️ 팀 커뮤니케이션 & 일일 스크럼 그라운드 룰
* [ ] **일일 스크럼 (Daily Standup)**:
  * 매일 오전 또는 저녁 10분 진행 (1. 어제 한 일, 2. 오늘 할 일, 3. 현재 겪는 블로커/장애물 공유)
* [ ] **소통 채널 분리 (Discord 또는 Slack)**:
  * `#공지` (마일스톤, 긴급 사항)
  * `#github-bot` (PR/Issue 실시간 알림 웹훅 연동)
  * `#api-연동-공유` (API 스펙 변경점 즉시 전파)
