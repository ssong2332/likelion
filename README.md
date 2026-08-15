# 🦁 매니패스트 (Manyfast) — 글로벌 업무 메시지 AI 어시스턴트

> **멋쟁이사자처럼 7일 초단기 해커톤 프로젝트**  
> **팀명**: 어떻게든 되겠조 | **개발 기간**: 2026년 8월 15일 ~ 8월 21일 (총 7일)  
> **GitHub Repository**: [https://github.com/ssong2332/likelion](https://github.com/ssong2332/likelion)

---

## 📚 핵심 프로젝트 문서 바로가기

| 문서명 | 주요 내용 | 링크 |
| :--- | :--- | :--- |
| 📅 **PROJECT_PLAN.md** | 4인 R&R, **백엔드 A/B 세부 기능 분담표**, 7일 간트 차트 및 GitHub 협업 브랜치 전략 | [바로가기](PROJECT_PLAN.md) |
| 🔌 **API_CONTRACT.md** | AI 교정(`POST /api/ai/analyze-refine`) 및 회신(`POST /api/ai/reply-draft`) Mock JSON 규격 | [바로가기](API_CONTRACT.md) |
| 📋 **PREPARATION_CHECKLIST.md** | 킥오프 전 필수 준비 체크리스트 및 심사위원용 킬러 데모 시나리오 2종 | [바로가기](PREPARATION_CHECKLIST.md) |
| 📝 **MANYFAST_SPEC.md** | 매니패스트 기획 요구사항 및 F-1 ~ F-8 기능 명세서 | [바로가기](MANYFAST_SPEC.md) |
| 📊 **SPEC_COMPARISON.md** | 초기 기획(사이)과 매니패스트 간의 상호 비교 분석서 | [바로가기](SPEC_COMPARISON.md) |
| 📂 **SAI_SPEC.md** | 초기 사이 (Sai) 통합 명세서 백업본 | [바로가기](SAI_SPEC.md) |

---

## 1. 📌 프로젝트 개요 (Product Vision)

글로벌 협업 실무자가 업무 요청의 **담당자·기한·긴급도·비즈니스 영향**을 잃지 않으면서도, 상대방의 **언어·협업 성향·팀 규칙**에 맞는 명확하고 예의 있는 메시지를 빠르게 작성하고 수신 메시지에 대한 회신을 생성하도록 돕는 지능형 웹 에디터입니다.

### 💡 해결하고자 하는 문제 (Problem)
* **단순 번역의 한계**: 직설적인 표현은 비난이나 명령으로 오해되어 관계를 해치고, 지나치게 완곡한 번역은 요청의 우선순위와 마감 기한을 흐려 일정 차질을 유발합니다.
* **시차 및 용어 오해**: 상대방의 비업무 시간(야간, 휴일) 발송으로 인한 협업 마찰과 팀/프로젝트 고유 용어의 임의 오역이 빈번합니다.

### 🎯 핵심 솔루션 (Solution)
1. **핵심 업무 정보 보존**: 목적, 마감일, 담당자, 긴급도, 비즈니스 영향을 추출하고 누락 시 작성 보완 가이드 제공.
2. **개인 성향 & 팀 용어집 우선 적용**: 국가별 고정관념 대신 사용자가 설정한 개인 협업 성향과 팀 규칙/용어집을 우선 반영.
3. **시차 및 비업무 시간 자동 환산**: 발신자/수신자 타임존을 비교하여 야간/휴일 발송 시 확인 가능 시점 안내.
4. **수신 메시지 회신 초안 생성**: 상대의 요청 사항을 분석하여 수락/일정조율/자료요청 등 대응 방향별 3가지 템플릿 제공.

---

## 2. 🧩 핵심 기능 명세 (F-1 ~ F-8)

* **F-1. 발신자·수신자 상호 시간대 고려**: 날짜/시간/기한 자동 환산 및 상대방 비업무 시간(야간/주말) 감지 안내
* **F-2. 핵심 업무 정보 분석 및 누락 안내**: 필수 정보(목적/기한 등) 식별, 누락 알림 및 직설적 오해 유발 표현 감지
* **F-3. 맥락 기반 교정 및 보완 문구 제안**: 개인 성향/팀 용어집을 주입한 맞춤 교정안 및 누락 정보 채우기 문구 생성
* **F-4. 원문-수정안 비교, 직접 편집 및 최종 승인**: 단어/문장 차이점 및 수정 이유(Reasoning) 대조, 유저 직접 수정 및 승인
* **F-5. 커뮤니케이션 규칙 및 용어집 관리**: 개인/팀 규칙 및 용어 사전 CRUD 관리 및 교정 프롬프트 동적 주입
* **F-6. 개인 협업 성향 설정 관리**: 기본 협업 성향 CRUD 및 메시지별 임시 성향 오버라이드
* **F-7. 메시지 데이터 보관 및 삭제 제어**: 저장된 메시지 데이터 보관 현황 조회 및 개별/일괄 영구 삭제 제어
* **F-8. 수신 메시지 기반 회신 초안 보조**: 수신 요청 분석 후 대응 방향별 3가지 회신 초안 자동 생성 (플레이스홀더 포함)

---

## 3. 👥 팀 구성 및 역할 분담 (R&R)

| 역할 | 담당자 | 핵심 담당 업무 및 구현 기능 |
| :--- | :--- | :--- |
| 🎨 **디자이너** | 1명 | • UI/UX 디자인, 기획 에디터 및 비교 뷰 레이아웃 설계 (F-4)<br>• 디자인 시스템 구축 및 피그마 에셋 전달<br>• 최종 데모 발표자료(Deck) 제작 |
| 💻 **프론트엔드** | 1명 | • React/Vite 기반 웹 에디터 인터랙션 및 상태 관리<br>• 원문-수정문 비교 패널 및 최종 승인/복사 뷰 (F-4)<br>• 시차 환산 UI, 용어집/성향 관리 뷰 (F-1, F-5, F-6) |
| 🧠 **백엔드 A (AI/LLM)** | 1명 | • OpenAI Structured Outputs 기반 교정 API (`POST /api/ai/analyze-refine`) (F-2, F-3, F-4.1)<br>• 수신 메시지 회신 초안 생성 API (`POST /api/ai/reply-draft`) (F-8)<br>• 프롬프트 엔지니어링 및 응답 지연시간(Latency) 최적화 |
| ⚙️ **백엔드 B (Infra/DB)** | 1명 | • 시차 연산 및 비업무 시간 판정 API (`POST /api/timezone/*`) (F-1)<br>• 용어집, 규칙, 협업 성향 CRUD API (F-5, F-6)<br>• 메시지 데이터 보관 현황 및 영구 삭제 API (F-7)<br>• DB 스키마 설계 및 클라우드 호스팅 서버 배포 |

---

## 4. 🛠️ 기술 스택 및 배포 인프라 (Tech Stack & Infra)

* **Frontend**: React 18, Vite, Lucide React, Pretendard
* **Backend**: **Spring Boot 3.3 (Java 17, Gradle)**, Spring Web, Spring Data JPA, Lombok
* **AI Engine**: **OpenAI API (`gpt-4o-mini`)** (JSON Schema Structured Outputs)
* **Cloud & Server**: **가비아 클라우드 서버 (Gabia Cloud)** (Docker 컨테이너 또는 JAR 직접 실행), Nginx (Reverse Proxy & SSL)
* **Database**: **MySQL**, Spring Data JPA, Flyway (가비아 클라우드 연동 DB)
* **Collaboration**: GitHub Monorepo (`develop`, `feat/*` 브랜치 전략)

---

## 5. 🌿 GitHub 협업 브랜치 전략

```
main (실배포)
  └── develop (통합 개발)
        ├── feat/fe/f4-compare-panel       # 💻 프론트엔드
        ├── feat/be-a/f2-ai-refine         # 🧠 백엔드 A (AI 교정)
        ├── feat/be-a/f8-reply-draft       # 🧠 백엔드 A (회신 생성)
        ├── feat/be-b/f1-timezone          # ⚙️ 백엔드 B (시차 연산)
        ├── feat/be-b/f5-glossary-crud     # ⚙️ 백엔드 B (용어집 CRUD)
        ├── feat/be-b/f7-message-history   # ⚙️ 백엔드 B (데이터 삭제)
        └── feat/design/figma-assets       # 🎨 디자이너
```

---

## 6. 🚀 빠른 시작 (Getting Started)

### 저장소 복제 및 본인 브랜치 이동
```bash
# 1. 저장소 클론
git clone https://github.com/ssong2332/likelion.git
cd likelion

# 2. 본인 역할에 맞는 브랜치로 전환 (예시: 백엔드 A)
git checkout feat/be-a/f2-ai-refine
```

### 백엔드 로컬 DB 환경변수 (Windows PowerShell)

MySQL에 `manyfast` 데이터베이스와 해당 데이터베이스에 접근 가능한 계정을 준비한 뒤 다음 환경변수를 설정합니다. Flyway가 스키마를 관리하므로 애플리케이션 계정에는 migration 실행에 필요한 권한이 있어야 합니다.

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/manyfast?serverTimezone=UTC&characterEncoding=UTF-8"
$env:DB_USERNAME = "manyfast"
$env:DB_PASSWORD = "<local-password>"

cd server
.\gradlew.bat bootRun
```

실제 비밀번호와 운영 DB 접속정보는 저장소에 커밋하지 않습니다.
