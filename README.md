# 구독 가성비 트래커 (Subscription Value Tracker)

구독 서비스와 고정 투자 항목의 실제 사용 대비 가성비를 자동 계산하는 웹 애플리케이션입니다.

> **핵심 가치**: "출석할수록 금액이 내려가는 걸 눈으로 보면서 뿌듯함 느끼기"

## 주요 기능

### 구독 관리
- 헬스장, 넷플릭스 등 월정액 서비스 등록/수정/삭제
- 출석 체크로 사용 기록 관리
- **회당 비용 자동 계산**: 총 금액 / 총 사용 횟수

### 캘린더 뷰
- 월간 달력에 출석 기록 및 회당 비용 표시
- 색상 코딩: 초록(가성비 좋음) / 노랑(보통) / 빨강(경고)
- 날짜 클릭으로 과거 날짜에도 출석 체크 가능

### 투자 관리
- 전자책 리더기, 연간 이용권 등 일회성 투자 항목 관리
- 손익분기점 및 총 절약액 계산
- 사용 기록별 절약 금액 추적

### 통계 대시보드
- 월별 사용 횟수 추이 차트
- 구독별 월 비용 비교 차트
- 월별 투자 절약액 차트

### UI/UX
- 다크/라이트 모드 전환 (설정 저장)
- 반응형 모바일 UI

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.x |
| Template Engine | Thymeleaf + HTMX |
| Database | H2 (개발) / MySQL (운영) |
| ORM | Spring Data JPA |
| Styling | Tailwind CSS (CDN) |
| Charts | Chart.js |
| Build | Gradle (Groovy DSL) |

## 시작하기

### 요구사항
- Java 21 이상
- Gradle 8.x 이상

### 설치 및 실행

```bash
# 저장소 클론
git clone https://github.com/your-username/subscription-value-tracker.git
cd subscription-value-tracker

# 개발 서버 실행
./gradlew bootRun

# 브라우저에서 접속
open http://localhost:8080
```

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests "SubscriptionServiceTest"
```

### 빌드

```bash
./gradlew build

# JAR 파일 실행
java -jar build/libs/subscription-value-tracker-0.0.1-SNAPSHOT.jar
```

## 프로젝트 구조

```
src/main/java/com/tracker/subscriptionvaluetracker/
├── common/                 # 공통 유틸리티
│   ├── EmojiMapper.java   # 이모지 코드 → 이모지 변환
│   └── UserIdentifier.java # UUID 쿠키 기반 사용자 식별
├── domain/
│   ├── subscription/       # 구독 도메인
│   │   ├── Subscription.java
│   │   ├── UsageLog.java
│   │   ├── SubscriptionRepository.java
│   │   ├── SubscriptionService.java
│   │   ├── CalendarService.java
│   │   └── SubscriptionController.java
│   └── investment/         # 투자 도메인
│       ├── Investment.java
│       ├── InvestmentUsage.java
│       ├── InvestmentRepository.java
│       ├── InvestmentService.java
│       └── InvestmentController.java
└── web/                    # 웹 컨트롤러
    ├── DashboardController.java
    ├── CalendarController.java
    ├── StatisticsController.java
    └── StatisticsService.java

src/main/resources/
├── templates/              # Thymeleaf 템플릿
│   ├── layout/            # 공통 레이아웃
│   ├── subscription/      # 구독 CRUD 페이지
│   ├── investment/        # 투자 CRUD 페이지
│   └── fragments/         # 재사용 컴포넌트
└── application.yml        # 설정 파일
```

## API 명세

### 구독 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/` | 대시보드 (구독 목록) |
| GET | `/subscriptions/new` | 구독 등록 폼 |
| POST | `/subscriptions` | 구독 등록 |
| GET | `/subscriptions/{id}` | 구독 상세 |
| GET | `/subscriptions/{id}/edit` | 구독 수정 폼 |
| POST | `/subscriptions/{id}` | 구독 수정 |
| POST | `/subscriptions/{id}/delete` | 구독 삭제 |
| POST | `/subscriptions/{id}/check-in` | 출석 체크/취소 |

### 캘린더 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/calendar` | 캘린더 페이지 |
| GET | `/calendar/grid` | 캘린더 그리드 (HTMX) |

### 투자 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/investments` | 투자 목록 |
| GET | `/investments/new` | 투자 등록 폼 |
| POST | `/investments` | 투자 등록 |
| GET | `/investments/{id}` | 투자 상세 |
| POST | `/investments/{id}/usage` | 사용 기록 추가 |

### 통계 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/stats` | 통계 페이지 |
| GET | `/stats/api/monthly-usage` | 월별 사용 횟수 데이터 |
| GET | `/stats/api/cost-comparison` | 구독별 비용 데이터 |
| GET | `/stats/api/investment-savings` | 투자 절약액 데이터 |

## 사용자 식별

- 로그인 없이 UUID + 쿠키 방식으로 사용자 식별
- 쿠키 유효기간: 30일
- 쿠키 삭제 시 데이터 접근 불가

## 개발 가이드

### 브랜치 전략

- `master`: 안정 버전
- `dev`: 개발 통합 브랜치
- `feature/*`: 기능별 브랜치

### 커밋 메시지 형식 (한글)

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

## 라이선스

MIT License

## 기여

이슈와 PR은 언제든 환영합니다!
