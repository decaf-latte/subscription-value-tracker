# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

구독 가성비 트래커 (Subscription Value Tracker) - A web application that calculates the cost-per-use value of subscription services and fixed investments. Core concept: "출석할수록 금액이 내려가는 걸 눈으로 보면서 뿌듯함 느끼기" (Feel motivated watching daily costs decrease with each usage).

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4.x
- **Template Engine**: Thymeleaf + HTMX (no separate frontend build)
- **Database**: H2 (dev) / MySQL (prod)
- **ORM**: Spring Data JPA
- **Styling**: Tailwind CSS (CDN)
- **Build Tool**: Gradle (Groovy DSL)

## Common Commands

```bash
# Run development server
./gradlew bootRun

# Run tests
./gradlew test

# Build
./gradlew build

# Access application at http://localhost:8080
```

## Architecture

Single Spring Boot project with server-side rendering:

```
src/main/java/com/tracker/subscriptionvaluetracker/
├── domain/
│   ├── subscription/     # Subscription entity, repository, service, controller
│   └── investment/       # Investment entity, repository, service, controller
└── web/                  # Dashboard and page controllers

src/main/resources/
├── templates/            # Thymeleaf templates
│   ├── layout/          # Common layouts
│   ├── subscription/    # Subscription CRUD views
│   ├── investment/      # Investment CRUD views
│   └── fragments/       # Reusable components
└── static/              # Static assets
```

## Key Domain Concepts

### Subscription (구독)
- Monthly subscription services (gym, Netflix, etc.)
- Tracks usage/attendance via `UsageLog`
- **Cost Per Use Calculation**: `(monthlyAmount × monthsSinceStart) ÷ totalUsageCount`
  - 구독 시작월부터 현재월까지의 총 지불액을 총 사용 횟수로 나눔
  - 예: 월 10,000원, 1월 4회 + 2월 1회 = 총 5회 → (10,000 × 2) / 5 = 4,000원/회
- All usage dates (across all months) show the same calculated cost (updates when new usage added)

### Investment (투자)
- One-time purchases with ongoing savings tracking (e-reader, annual pass)
- Tracks break-even point and total savings
- Compares original vs actual price per use

### User Identification
- UUID + Cookie-based (no login required)
- 30-day cookie expiration
- All queries filter by `user_uuid`

## Key Business Logic

- Use `BigDecimal` for all monetary calculations
- Daily cost color coding: Green (good value) / Yellow (normal) / Red (warning)
- Emoji codes stored in DB, converted to emoji on frontend (e.g., "gym" → "🏋️")
- HTMX for partial page updates on attendance check (refreshes entire calendar)

## Testing Requirements

**테스트 코드 작성 규칙 (필수!):**
- 모든 기능에는 반드시 테스트 코드(TC)를 작성해야 함
- 테스트는 JUnit 5 + Mockito 사용
- `@DisplayName`으로 한글 테스트 설명 작성
- `@Nested`로 테스트 그룹화

**테스트 구조:**
```
src/test/java/com/tracker/subscriptionvaluetracker/
├── domain/subscription/
│   ├── SubscriptionServiceTest.java    # 서비스 단위 테스트
│   ├── SubscriptionControllerTest.java # 컨트롤러 단위 테스트
│   └── CalendarServiceTest.java        # 캘린더 서비스 테스트
├── common/
│   ├── EmojiMapperTest.java            # 유틸리티 테스트
│   └── UserIdentifierTest.java         # 사용자 식별 테스트
└── web/
    └── CalendarControllerTest.java     # 웹 컨트롤러 테스트
```

**테스트 실행:**
```bash
./gradlew test                    # 전체 테스트 실행
./gradlew test --tests "클래스명" # 특정 클래스만 실행
```

## Git Workflow

**브랜치 전략:**
- `master`: 안정 버전
- `dev`: 개발 통합 브랜치
- `feature/*`: 기능별 브랜치 (예: `feature/subscription-crud`)

**작업 완료 시 Git 프로세스:**
1. 태스크별 feature 브랜치 생성: `git checkout -b feature/태스크명`
2. 기능 구현 + 테스트 코드 작성 (필수!)
3. 테스트 통과 확인: `./gradlew test`
4. 작업 완료 후 커밋: 작업 내용 요약을 커밋 메시지로
5. dev 브랜치로 머지 후 푸시
6. PRD.md, PROJECT_SPEC.md 체크리스트 업데이트
7. DEVELOPMENT.md 업데이트 (새 엔티티, API, 다이어그램 추가)

**커밋 메시지 형식 (한글로 작성!):**
```
[타입] 작업 요약

- 세부 변경사항 1
- 세부 변경사항 2
```

**타입:**
- `기능`: 새로운 기능 추가
- `수정`: 버그 수정
- `문서`: 문서 수정
- `리팩토링`: 코드 개선
- `테스트`: 테스트 추가/수정

## Reference Documents

- `PRD.md` - Product requirements document
- `PROJECT_SPEC.md` - Technical specifications with data models and API design
- `DEVELOPMENT.md` - 개발 문서 (패키지 구조, ERD, 플로우차트, 시퀀스 다이어그램)
- `wireframe-v4-calendar.html` - UI wireframe reference
