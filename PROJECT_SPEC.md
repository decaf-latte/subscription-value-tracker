# 구독 가성비 트래커 (Subscription Value Tracker)

> **📌 문서 관리 지침**
> 이 문서는 Claude Code가 프로젝트를 이해하고 개발하기 위한 기획서입니다.
> 사용자와의 대화에서 결정된 사항은 즉시 이 문서에 반영합니다.
> 다른 세션의 Claude도 이 문서만 보면 프로젝트 맥락을 파악할 수 있도록 유지합니다.

---

## 프로젝트 개요

구독 서비스와 고정 투자 항목의 실제 사용 대비 가성비를 자동 계산하는 웹 애플리케이션

### 목표
- 월정액 구독 서비스의 일일 비용 실시간 계산
- 고정 투자 항목(이북 리더기 등)의 손익분기점 및 절약액 추적
- 사용 동기 부여를 위한 시각화 제공

---

## 기술 스택

### Backend + Frontend (통합)
- **Language**: Java 21
- **Framework**: Spring Boot 4.x
- **Template Engine**: Thymeleaf
- **인터랙션**: HTMX (페이지 새로고침 없이 부분 업데이트)
- **Build Tool**: Gradle (Groovy DSL)
- **Database**: H2 (개발) / MySQL (운영)
- **ORM**: Spring Data JPA
- **스타일링**: Tailwind CSS (CDN)

### Infrastructure
- 단일 서버 (Spring Boot)
- 배포: Railway, Render, 또는 NCP

### 아키텍처 결정 사항
- **단일 프로젝트 구조**: 백엔드/프론트엔드 하나의 Spring Boot 프로젝트로 통합
- **서버 사이드 렌더링**: Thymeleaf로 HTML 렌더링
- **HTMX**: 출석 체크 등 인터랙션은 HTMX로 부분 업데이트 (페이지 새로고침 없이)
- **빠른 개발**: 프론트엔드 빌드 과정 없음, 서버 하나만 실행하면 끝

### 기술 스택 변경 이유 (2025-01-06)
- React → Thymeleaf: 러닝커브 감소, 1월 내 완성 목표
- 모노레포 → 단일 프로젝트: 구조 단순화, 배포 용이

---

## 핵심 기능

### 1. 구독형 항목 (Subscription Type)

월정액 서비스의 출석/사용 기반 가성비 계산

#### 데이터 모델
```java
// 구독 (Subscription)
@Entity
@Table(name = "subscription")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String userUuid;           // 사용자 식별 (쿠키 기반 UUID)
    
    private String name;               // 서비스명 (헬스장)
    private String emojiCode;          // 이모지 코드 (gym, netflix, book 등)
    
    private String periodType;         // 기간 유형 - 직접 입력 (월간, 3개월, 12개월, 30회 등)
    
    private BigDecimal totalAmount;    // 총 금액 (360,000원)
    private BigDecimal monthlyAmount;  // 월 환산 금액 (30,000원) - 자동계산
    
    private LocalDate startDate;       // 시작일
    private LocalDate endDate;         // 종료일 (선택)
    
    private Boolean isActive = true;
    
    // getters, setters, constructors
}

// 출석 기록 (UsageLog)
@Entity
@Table(name = "usage_log")
public class UsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long subscriptionId;       // FK (논리적으로만, 물리적 제약 X)
    
    private LocalDate usedAt;          // 사용 날짜
    private String note;               // 메모 (선택)
    
    // getters, setters, constructors
}
```

#### 사용자 식별 (로그인 없이)
```java
// UUID + 쿠키 방식
public class UserIdentifier {
    private static final String COOKIE_NAME = "user_uuid";
    private static final int MAX_AGE = 60 * 60 * 24 * 30;  // 30일
    
    public static String getUserUuid(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // 없으면 새로 생성
        String uuid = UUID.randomUUID().toString();
        Cookie newCookie = new Cookie(COOKIE_NAME, uuid);
        newCookie.setMaxAge(MAX_AGE);
        newCookie.setPath("/");
        response.addCookie(newCookie);
        return uuid;
    }
}
```
- 첫 접속 시 UUID 생성 → 쿠키에 30일간 저장
- 모든 DB 조회 시 `WHERE user_uuid = ?` 조건 추가
- 한계: 쿠키 삭제/다른 기기 → 새 사용자 취급 (테스트 기간용)

#### 이모지 코드 매핑 (프론트에서 변환)
```java
public class EmojiMapper {
    private static final Map<String, String> emojiMap = new HashMap<>();
    
    static {
        emojiMap.put("gym", "🏋️");
        emojiMap.put("netflix", "🎬");
        emojiMap.put("youtube", "📺");
        emojiMap.put("book", "📚");
        emojiMap.put("ebook", "📖");
        emojiMap.put("music", "🎵");
        emojiMap.put("game", "🎮");
        emojiMap.put("coffee", "☕");
        emojiMap.put("swim", "🏊");
        emojiMap.put("pilates", "🧘");
        emojiMap.put("language", "🗣️");
        emojiMap.put("default", "📌");
    }
    
    public static String toEmoji(String code) {
        return emojiMap.getOrDefault(code, emojiMap.get("default"));
    }
    
    public static Map<String, String> getAllCodes() {
        return Collections.unmodifiableMap(emojiMap);
    }
}
```

#### MySQL 스키마
```sql
CREATE TABLE subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_uuid VARCHAR(36) NOT NULL,           -- 사용자 식별 UUID
    name VARCHAR(100) NOT NULL,
    emoji_code VARCHAR(50) NOT NULL,          -- 이모지 코드 (gym, netflix 등)
    period_type VARCHAR(50) NOT NULL,         -- 직접 입력 (월간, 3개월, 12개월, 30회 등)
    total_amount DECIMAL(10, 0) NOT NULL,
    monthly_amount DECIMAL(10, 0) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE usage_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,          -- FK (논리적으로만, 물리적 제약 X)
    used_at DATE NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_subscription_user ON subscription(user_uuid);
CREATE INDEX idx_usage_subscription_date ON usage_log(subscription_id, used_at);
CREATE INDEX idx_usage_used_at ON usage_log(used_at);
```

#### 구독 추가 폼 필드
| 필드 | 타입 | 설명 |
|------|------|------|
| 이름 | 텍스트 | 헬스장, 넷플릭스 등 |
| 이모지 | 셀렉트 | gym, netflix, book 등 코드 선택 |
| 기간 유형 | 텍스트 | 직접 입력 (월간, 3개월, 12개월, 30회 등) |
| 금액 | 숫자 | 총 금액 |
| 시작일 | 날짜 | 구독 시작일 |

#### 계산 로직
```kotlin
// 이번 달 일일 비용
fun calculateDailyCost(subscription: Subscription, usageCount: Int): BigDecimal {
    if (usageCount == 0) return subscription.monthlyFee
    return subscription.monthlyFee.divide(BigDecimal(usageCount), 2, RoundingMode.HALF_UP)
}

// 남은 일수 대비 목표 달성률
fun calculateTargetProgress(targetDays: Int, currentUsage: Int, remainingDays: Int): ProgressStatus {
    val remaining = targetDays - currentUsage
    return ProgressStatus(
        achieved = currentUsage,
        target = targetDays,
        remainingDays = remainingDays,
        onTrack = remaining <= remainingDays
    )
}
```

#### 화면 요구사항
- 구독 서비스 목록 (카드 형태)
- 각 카드에 표시: 서비스명, 월비용, 이번 달 사용횟수, 현재 일일비용
- 출석 체크 버튼 (원터치)
- **캘린더 뷰**: 월간 달력에서 출석/사용 기록을 이모지 + 일일비용으로 표시
  - 각 서비스별 사용자 지정 이모지 (🏋️ 헬스장, 🎬 넷플릭스, 📚 밀리의서재, 📖 이북리더기)
  - **모든 출석일에 동일한 일일비용 표시** (출석 추가 시 전체 업데이트)
  - 날짜 클릭 시 해당 날짜의 상세 기록 표시
  - 범례로 어떤 이모지가 어떤 서비스인지 표시
  - 일일비용 색상: 초록(가성비 좋음) / 노랑(보통) / 빨강(경고)

#### 일일비용 계산 핵심 로직
```
일일비용 = 월 구독료 ÷ 이번 달 총 사용 횟수
```
- 출석할수록 일일비용 감소 → **모든 출석일의 금액이 동시에 업데이트**
- 예: 헬스장 6회 출석 → 모든 출석일 ₩5,000 표시
- 7회째 출석 시 → 모든 출석일 ₩4,286으로 즉시 변경
- **HTMX로 출석 버튼 클릭 시 캘린더 전체 새로고침**

---

### 2. 투자형 항목 (Investment Type)

초기 투자 비용 대비 절약액 추적 (손익분기점 계산)

#### 데이터 모델
```kotlin
data class Investment(
    val id: Long,
    val name: String,                    // 항목명 (예: "크레마 카르타")
    val purchasePrice: BigDecimal,       // 구매 가격
    val purchaseDate: LocalDate,         // 구매일
    val category: InvestmentCategory,
    val comparisonBaseline: BigDecimal   // 비교 기준 단가 (예: 종이책 평균가)
)

data class InvestmentUsage(
    val id: Long,
    val investmentId: Long,
    val usedAt: LocalDate,
    val itemName: String,                // 사용 항목명 (예: 책 제목)
    val originalPrice: BigDecimal,       // 원래 가격 (종이책)
    val actualPrice: BigDecimal,         // 실제 지불 가격 (전자책/무료)
    val note: String?
)

enum class InvestmentCategory {
    E_READER,         // 이북 리더기
    ANNUAL_PASS,      // 연간 이용권
    EQUIPMENT,        // 장비 (운동기구 등)
    OTHER
}
```

#### 계산 로직
```kotlin
// 총 절약액
fun calculateTotalSavings(investment: Investment, usages: List<InvestmentUsage>): BigDecimal {
    return usages.sumOf { it.originalPrice - it.actualPrice }
}

// 순이익 (손익분기점 돌파 여부)
fun calculateNetProfit(investment: Investment, usages: List<InvestmentUsage>): BigDecimal {
    val totalSavings = calculateTotalSavings(investment, usages)
    return totalSavings - investment.purchasePrice
}

// 손익분기점까지 남은 금액
fun calculateBreakEvenRemaining(investment: Investment, usages: List<InvestmentUsage>): BreakEvenStatus {
    val netProfit = calculateNetProfit(investment, usages)
    return BreakEvenStatus(
        isBreakEven = netProfit >= BigDecimal.ZERO,
        remaining = if (netProfit < BigDecimal.ZERO) netProfit.abs() else BigDecimal.ZERO,
        totalSavings = calculateTotalSavings(investment, usages),
        usageCount = usages.size
    )
}

// 예상 손익분기점 도달일 (평균 사용 패턴 기반)
fun estimateBreakEvenDate(investment: Investment, usages: List<InvestmentUsage>): LocalDate? {
    if (usages.isEmpty()) return null
    
    val avgSavingsPerItem = calculateTotalSavings(investment, usages) / BigDecimal(usages.size)
    val remainingToBreakEven = investment.purchasePrice - calculateTotalSavings(investment, usages)
    
    if (remainingToBreakEven <= BigDecimal.ZERO) return LocalDate.now()
    
    val itemsNeeded = remainingToBreakEven.divide(avgSavingsPerItem, 0, RoundingMode.CEILING).toInt()
    val daysBetweenUsage = ChronoUnit.DAYS.between(usages.first().usedAt, usages.last().usedAt) / usages.size
    
    return LocalDate.now().plusDays(itemsNeeded * daysBetweenUsage)
}
```

#### 이북 리더기 특화 기능
```kotlin
// 전자책 vs 종이책 가격 비교 입력
data class BookUsage(
    val title: String,
    val paperPrice: BigDecimal,          // 종이책 정가
    val ebookPrice: BigDecimal,          // 전자책 구매가 (0이면 구독/무료)
    val source: BookSource               // 구매처
)

enum class BookSource {
    RIDI,
    RIDI_SELECT,      // 구독
    MILLIE,           // 밀리의서재
    KINDLE,
    YES24,
    OTHER
}
```

#### 화면 요구사항
- 투자 항목 카드: 구매가, 현재 절약액, 손익분기점 진행률 프로그레스바
- 손익분기점 돌파 시 축하 효과
- 사용 내역 리스트 (최근순)
- 월별 절약액 차트

---

## API 설계

### Subscription API
```
GET    /api/subscriptions                    # 목록 조회
POST   /api/subscriptions                    # 등록
GET    /api/subscriptions/{id}               # 상세 조회
PUT    /api/subscriptions/{id}               # 수정
DELETE /api/subscriptions/{id}               # 삭제

POST   /api/subscriptions/{id}/check-in      # 출석 체크
GET    /api/subscriptions/{id}/usage         # 사용 내역
GET    /api/subscriptions/{id}/stats         # 통계 (일일비용, 진행률)
```

### Investment API
```
GET    /api/investments                      # 목록 조회
POST   /api/investments                      # 등록
GET    /api/investments/{id}                 # 상세 조회
PUT    /api/investments/{id}                 # 수정
DELETE /api/investments/{id}                 # 삭제

POST   /api/investments/{id}/usage           # 사용 기록 추가
GET    /api/investments/{id}/usage           # 사용 내역
GET    /api/investments/{id}/break-even      # 손익분기점 상태
```

### Dashboard API
```
GET    /api/dashboard/summary                # 전체 요약
GET    /api/dashboard/monthly-report         # 월간 리포트
```

---

## 프로젝트 구조 (단일 프로젝트)

```
subscription-value-tracker/
├── PROJECT_SPEC.md                    # 📌 이 문서 (Claude 학습용 기획서)
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
├── docker-compose.yml                 # DB 등 로컬 개발 환경
└── src/
    ├── main/
    │   ├── kotlin/
    │   │   └── com/tracker/
    │   │       ├── SubscriptionValueTrackerApplication.kt
    │   │       ├── domain/
    │   │       │   ├── subscription/
    │   │       │   │   ├── Subscription.kt
    │   │       │   │   ├── UsageLog.kt
    │   │       │   │   ├── SubscriptionRepository.kt
    │   │       │   │   ├── SubscriptionService.kt
    │   │       │   │   └── SubscriptionController.kt
    │   │       │   └── investment/
    │   │       │       ├── Investment.kt
    │   │       │       ├── InvestmentUsage.kt
    │   │       │       ├── InvestmentRepository.kt
    │   │       │       ├── InvestmentService.kt
    │   │       │       └── InvestmentController.kt
    │   │       └── web/
    │   │           └── DashboardController.kt
    │   └── resources/
    │       ├── application.yml
    │       ├── templates/             # Thymeleaf 템플릿
    │       │   ├── layout/
    │       │   │   └── default.html   # 공통 레이아웃
    │       │   ├── index.html         # 대시보드
    │       │   ├── subscription/
    │       │   │   ├── list.html
    │       │   │   └── form.html
    │       │   ├── investment/
    │       │   │   ├── list.html
    │       │   │   └── form.html
    │       │   └── fragments/         # 재사용 컴포넌트
    │       │       ├── card.html
    │       │       └── chart.html
    │       └── static/
    │           └── css/
    │               └── custom.css     # 추가 스타일 (필요시)
    └── test/
        └── kotlin/
```

---

## 개발 단계 (1월 내 완성 목표)

### 🎯 1차 목표: 일일 비용 계산
> 구독 서비스의 출석 기록 + 일일 비용 자동 계산이 핵심

### 1주차 (1/6~12): 프로젝트 셋업 + 구독 기본
- [x] Spring Boot + Java 21 + Gradle 프로젝트 생성
- [x] Thymeleaf + Tailwind CSS + HTMX 설정
- [x] application.yml 설정 (H2 개발 / MySQL 운영 프로필 분리)
- [x] UserIdentifier 유틸리티 (UUID 쿠키 기반 사용자 식별)
- [x] EmojiMapper 유틸리티 (이모지 코드 변환)
- [x] 기본 레이아웃 템플릿 + 대시보드 페이지 템플릿
- [x] 구독 엔티티 + Repository (Subscription, UsageLog)
- [x] 구독 CRUD (등록/수정/삭제)
- [x] 출석 체크 기능 (HTMX로 버튼 클릭)
- [x] SubscriptionService (일일비용 계산, 출석 체크 로직)
- [x] DashboardController + SubscriptionController

### 2주차 (1/13~19): 핵심 기능 + UI
- [ ] 일일 비용 계산 로직
- [ ] **캘린더 뷰 구현** (출석/독서 기록 한눈에 보기)
- [ ] 대시보드 페이지 (요약 카드)
- [ ] 구독 카드 UI (색상 로직 적용)

### 3주차 (1/20~26): 투자형 추가
- [ ] 투자형 항목 CRUD
- [ ] 손익분기점 계산
- [ ] 이북 리더기 특화 입력 폼
- [ ] 투자형 카드 UI + 프로그레스 바

### 4주차 (1/27~31): 마무리 + 배포
- [ ] 차트 추가 (Chart.js)
- [ ] 반응형 모바일 UI
- [ ] 배포 (Railway 또는 Render)
- [ ] README 작성

---

## 실행 방법

```bash
# 개발 환경 실행 (서버 하나만 띄우면 끝!)
./gradlew bootRun

# 브라우저에서 접속
http://localhost:8080

# 테스트
./gradlew test

# 빌드
./gradlew build

# Docker로 실행
docker-compose up -d
```

---

## 참고 사항

- 금액 계산은 `BigDecimal` 사용 (부동소수점 오차 방지)
- 날짜/시간은 `java.time` 패키지 사용
- 테스트 코드 필수 (Service 레이어 중심)
- API 응답은 일관된 형식 사용

```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String?
)
```

---

## 변경 이력

| 날짜 | 변경 내용 |
|------|----------|
| 2025-01-06 | 최초 문서 작성 |
| 2025-01-06 | 프론트엔드 스택 React + TypeScript로 확정 |
| 2025-01-06 | 모노레포 구조로 변경 (backend/, frontend/ 분리) |
| 2025-01-06 | 문서 관리 지침 추가 (다른 세션 Claude도 이해할 수 있도록 지속 업데이트) |
| 2025-01-06 | **기술 스택 변경: React → Thymeleaf + HTMX** (1월 내 완성 목표) |
| 2025-01-06 | **구조 변경: 모노레포 → 단일 Spring Boot 프로젝트** |
| 2025-01-06 | 개발 일정 구체화 (주차별 목표 설정) |
| 2025-01-06 | **1차 목표: 일일 비용 계산** 으로 설정 |
| 2025-01-06 | **캘린더 뷰 추가**: 출석/독서 기록을 한눈에 볼 수 있는 월간 달력 |
| 2025-01-06 | **캘린더 v4**: 이모지 + 일일비용 표시, 모든 출석일 동일 금액 표시 |
| 2025-01-06 | **핵심 UX**: 출석할수록 금액이 내려가는 걸 시각적으로 느끼기 |
| 2025-01-06 | **DB 변경**: PostgreSQL → MySQL |
| 2025-01-06 | **구독 추가 폼**: 이름, 이모지, 기간유형(월간/3개월/6개월/12개월/횟수권), 금액, 시작일 |
| 2025-01-06 | **데이터 모델 상세화**: Subscription, UsageLog 엔티티 + MySQL 스키마 |
| 2025-01-06 | **period_type**: enum 제거 → String 직접 입력 |
| 2025-01-06 | **FK**: 물리적 제약 제거 → 논리적으로만 관리 |
| 2025-01-06 | **이모지**: 이모지 직접 저장 → 이모지 코드(gym, netflix 등) 저장 후 프론트에서 변환 |
| 2025-01-06 | **사용자 식별**: UUID + 쿠키 방식 (로그인 없이 30일간 사용자 구분) |
| 2025-01-08 | **언어 변경**: Kotlin → Java 21 |
| 2025-01-08 | **설정 완료**: application.yml (H2/MySQL), Thymeleaf 레이아웃, HTMX 연동 |
| 2025-01-08 | **유틸리티 생성**: UserIdentifier (UUID 쿠키), EmojiMapper (이모지 코드 변환) |
| 2025-01-08 | **템플릿 생성**: layout/default.html, index.html (대시보드) |
| 2025-01-08 | **구독 CRUD 완성**: Subscription/UsageLog 엔티티, Repository, Service, Controller |
| 2025-01-08 | **템플릿 추가**: subscription/list.html, subscription/form.html |
