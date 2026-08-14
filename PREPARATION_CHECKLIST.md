# 📋 해커톤 킥오프 전 필수 사전 준비 및 확정 기본값 (Preparation & Defaults)

본 문서는 멋쟁이사자처럼 7일 해커톤 동안 팀원 간 논의 비용을 없애고 즉시 개발에 착수할 수 있도록 **사전에 합의된 기본 설정값(Default Values)과 규격**을 정리한 문서입니다.

---

## 1. 🔌 API 통신 및 데이터 스키마 확정 기본값

* [x] **API Base URL 및 포트 규칙**:
  * 프론트엔드 로컬: `http://localhost:5173`
  * 백엔드(Spring Boot) 로컬: `http://localhost:8080`
  * 프론트엔드 프록시: `/api` 경로를 `http://localhost:8080`으로 포워딩 (`client/vite.config.js` 설정 완료)
* [x] **AI 교정 API (`POST /api/ai/analyze-refine`)**: [API_CONTRACT.md](API_CONTRACT.md#1-post-apiaanalyze-refine-ai-교정-및-분석) 규격 확정
* [x] **회신 생성 API (`POST /api/ai/reply-draft`)**: [API_CONTRACT.md](API_CONTRACT.md#2-post-apiareply-draft-수신-메시지-기반-회신-초안-생성---f-8) 규격 확정
* [x] **용어집/규칙/성향/삭제 CRUD API**: Spring Boot 도메인 컨트롤러 매핑 확정
* [x] **공통 에러 응답 포맷**:
  ```json
  {
    "status": 400,
    "error": "BAD_REQUEST",
    "message": "원문 메시지를 입력해 주세요."
  }
  ```
* [x] **날짜/시간 표준**: ISO-8601 UTC 문자열 (`YYYY-MM-DDTHH:mm:ssZ`)로 통신하고, 클라이언트에서 현지 시각 변환
* [x] **HTTP 통신 타임아웃(Timeout)**: 프론트엔드 `fetch`/`axios` 타임아웃 **30초 (30,000ms)** 설정

---

## 2. 🔑 외부 서비스 & 배포 인프라 확정 기본값

* [x] **AI 모델**: OpenAI **`gpt-4o-mini`** (응답 속도 1~2초, JSON Structured Outputs 적용)
* [x] **백엔드 서버 프레임워크**: **Spring Boot 3.3.3 (Java 17, Gradle)**
* [x] **서버 호스팅 환경**: **가비아 클라우드 서버 (Gabia Cloud)**
  * 배포 방식: `Dockerfile` 기반 멀티스테이지 경량 컨테이너 실행
  * 포트 및 웹서버: Nginx (80/443 포트 SSL) ➔ Spring Boot (내부 `8080` 포트) 리버스 프록시
* [x] **데이터베이스**: MySQL / PostgreSQL (가비아 클라우드 연동 DB)
* [x] **🧠 백엔드 A (AI) ↔ ⚙️ 백엔드 B (DB) 데이터 연동 규격**:
  * **프롬프트 주입 포맷**: 백엔드 B의 DB 데이터를 백엔드 A의 AI 프롬프트에 넘길 때 `[팀 용어집 규칙] {term} -> {rule} ({note})` 형식의 문자열로 결합
  * **LLM 장애 폴백 룰**: OpenAI API 호출 실패 시 1회 즉시 재시도 ➔ 재실패 시 서비스 중단을 막기 위해 [API_CONTRACT.md](API_CONTRACT.md)의 Mock 안전 템플릿으로 자동 폴백하여 프론트에 `200 OK` 응답
* [x] **환경변수 기본 템플릿 (`application.yml`)**:
  ```yaml
  server:
    port: 8080
  openai:
    api-key: ${OPENAI_API_KEY:}
    model: gpt-4o-mini
    api-url: https://api.openai.com/v1/chat/completions
  cors:
    allowed-origins:
      - "http://localhost:5173"
      - "http://localhost:3000"
  ```

---

## 3. 🎨 디자인 에셋 & UI 토큰 확정 기본값

* [x] **기준 해상도(Viewport)**: Desktop **`1440px`** (최소 지원 너비: `1280px`)
* [x] **컬러 시스템 (Design Tokens)**:
  * Primary Accent: `#FF6B00` (주황 - 버튼, 강조, 로고)
  * Primary Hover: `#E55E00`
  * Success Green: `#10B981` (초록 - 용어 적용, 성공 뱃지, 역번역)
  * Warning Yellow: `#F59E0B` (노랑 - 시차 야간 경고, 누락 알림)
  * Background: `#F9FAFB` (페이지 배경), `#FFFFFF` (카드 배경)
  * Text: `#111827` (본문 타이틀), `#6B7280` (설명/보조 텍스트)
  * Border: `#E5E7EB` (경계선)
* [x] **아이콘 라이브러리**: **`lucide-react`** 통일 (SVG 다운로드 없이 React 컴포넌트로 직결)
* [x] **타이포그래피(Font)**: **`Pretendard Variable`** (한국어/영어 통합 최적화)
* [x] **3가지 화면 상태 UI 가이드**:
  * **Loading**: AI 교정 대기 중 `Sparkles` 아이콘 + 펄스(Pulse) 스켈레톤 애니메이션
  * **Empty State**: 왼쪽 에디터 작성 전 우측 패널에 `Info` 아이콘과 "메시지를 입력하고 교정을 실행하세요" 안내 문구
  * **Error State**: 상단 슬라이드다운 빨간색 알림 토스트 (`AlertCircle` 아이콘)

---

## 4. 🎭 심사위원 시연용 '킬러 데모 시나리오' 2종 (고정본)

* [x] **시나리오 A (발신 모드: 일정 지연 및 블로커 해소 요청)**
  * **입력 원문 (KST)**: `"이거 리뷰 3일째 안 봐주셔서 오늘 배포 못 나갑니다. Manyfast 용어 확인해서 오늘 안에 피드백 주세요."`
  * **AI 감지 & 시연 포인트**:
    1. 감정적 비난 표현 감지 ➔ 사실 기반 정중한 문장으로 정제
    2. 수신자 현지 시각(EST 야간 03:00) 자동 감지 ➔ 시차 비업무 시간 경고 위젯 노출
    3. `Manyfast` 용어집 [원문 유지] 태그 자동 적용
    4. 역번역(한국어 검증) 상시 노출 및 원클릭 복사
* [x] **시나리오 B (수신 모드: 영문 완곡 표현 대응 회신 초안)**
  * **수신 원문 (EST)**: `"I have a few minor comments on your architecture draft. When you get a chance, let's discuss."`
  * **AI 감지 & 시연 포인트**:
    1. 숨은 실제 의도(아키텍처 전면 재검토 가능성) 요약 분석
    2. 3가지 대응 방향(즉시 수락 및 미팅 제안 / 사전 서면 코멘트 요청 / 일정 지연 조율) 템플릿 즉시 생성
    3. `[선호하는 시간]`, `[문서 링크]` 플레이스홀더 원클릭 복사

---

## 5. 🗣️ 협업 & 코드 관리 그라운드 룰

* [x] **일일 스탠드업 미팅**: 매일 **오전 10:00 (10분간 진행)**
  * 1. 어제 완료한 작업
  * 2. 오늘 진행할 작업
  * 3. 현재 겪고 있는 블로커(장애물) 공유
* [x] **GitHub 브랜치 보호 규칙**: `main` 브랜치 직접 푸시 전면 차단 (최소 1명 승인 PR 머지만 허용)
* [x] **피처 프리즈 (Feature Freeze)**: **8월 19일 20:00 KST**
  * 8/19 저녁 이후 신규 기능 개발 중단 ➔ 버그 핫픽스, UI 폴리싱, 발표 데모 연습에만 집중
* [x] **파트 간 작업 영역 격리**:
  * FE: `/client` 전담
  * BE A: `/server/src/main/java/.../ai` 전담
  * BE B: `/server/src/main/java/.../domain` 전담
  * Design: `/docs` 및 피그마 에셋 전달
