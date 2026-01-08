# PRD: 구독 가성비 트래커 (Subscription Value Tracker)

## 1. 개요

### 1.1 제품 요약
구독 서비스와 고정 투자 항목의 실제 사용 대비 가성비를 자동 계산하는 웹 애플리케이션

### 1.2 핵심 가치
**"출석할수록 금액이 내려가는 걸 눈으로 보면서 뿌듯함 느끼기"**

### 1.3 목표 사용자
- 헬스장, OTT 등 월정액 서비스 이용자
- 구독 서비스 가성비를 확인하고 싶은 사람
- 사용 동기 부여가 필요한 사람

### 1.4 성공 지표
- MVP 완성: 2025년 1월 내
- 베타 테스트: 1개월간 외부 사용자 테스트
- 핵심 기능 동작: 출석 체크 → 일일비용 즉시 업데이트

---

## 2. 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.x |
| Template | Thymeleaf + HTMX |
| Database | H2 (개발) / MySQL (운영) |
| ORM | Spring Data JPA |
| Styling | Tailwind CSS (CDN) |
| Build | Gradle (Groovy DSL) |

---

## 3. 핵심 기능

### 3.1 구독 관리 (CRUD)
- 구독 서비스 등록/수정/삭제
- 입력 필드: 이름, 이모지코드, 기간유형, 금액, 시작일

### 3.2 출석 체크
- 원터치 출석 버튼
- 출석 시 일일비용 즉시 재계산
- 모든 출석일 금액 동시 업데이트

### 3.3 캘린더 뷰
- 월간 달력에 출석 기록 표시
- 이모지 + 일일비용 표시
- 색상: 초록(가성비 좋음) / 노랑(보통) / 빨강(경고)

### 3.4 일일비용 계산
```
일일비용 = 월 환산 금액 ÷ 이번 달 총 사용 횟수
```

### 3.5 사용자 식별
- UUID + 쿠키 방식 (로그인 없음)
- 30일간 브라우저에 저장
- 베타 테스트 기간용

---

## 4. 데이터 모델

### 4.1 Subscription (구독)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT | PK |
| user_uuid | VARCHAR(36) | 사용자 식별 |
| name | VARCHAR(100) | 서비스명 |
| emoji_code | VARCHAR(50) | 이모지 코드 |
| period_type | VARCHAR(50) | 기간 유형 (직접 입력) |
| total_amount | DECIMAL | 총 금액 |
| monthly_amount | DECIMAL | 월 환산 금액 |
| start_date | DATE | 시작일 |
| end_date | DATE | 종료일 (선택) |
| is_active | BOOLEAN | 활성 상태 |

### 4.2 UsageLog (출석 기록)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT | PK |
| subscription_id | BIGINT | FK (논리적) |
| used_at | DATE | 사용 날짜 |
| note | VARCHAR(255) | 메모 |

---

## 5. UI/UX 요구사항

### 5.1 레이아웃
- 상단: 요약 카드 (4컬럼)
- 중앙: 캘린더 뷰 (풀 width)
- 하단: 구독 카드 + 출석 버튼 (4컬럼)

### 5.2 캘린더 셀
```
┌─────────────────┐
│ 6        [오늘] │
│ 🏋️ ₩5,000     │
│ 📚 ₩3,300     │
└─────────────────┘
```

### 5.3 디자인
- 다크모드 기반
- Tailwind CSS
- 모바일 반응형

---

## 6. 개발 일정

### Phase 1: MVP (1월)

#### 1주차 (1/6~12): 프로젝트 셋업 + 구독 기본
- [x] Spring Boot 프로젝트 생성
- [x] Thymeleaf + Tailwind 설정
- [x] MySQL 연동 (H2 개발 / MySQL 운영 프로필 분리)
- [x] Subscription 엔티티 + Repository
- [x] 구독 CRUD API + 화면
- [x] UUID 쿠키 기반 사용자 식별

#### 2주차 (1/13~19): 핵심 기능 + UI
- [x] UsageLog 엔티티 + Repository
- [x] 출석 체크 기능 (HTMX)
- [ ] 일일비용 계산 로직
- [ ] 캘린더 뷰 구현
- [ ] 대시보드 요약 카드

#### 3주차 (1/20~26): 투자형 추가
- [ ] Investment 엔티티
- [ ] 손익분기점 계산
- [ ] 이북 리더기 특화 입력
- [ ] 투자형 카드 UI

#### 4주차 (1/27~31): 마무리 + 배포
- [ ] Chart.js 차트 추가
- [ ] 반응형 모바일 UI
- [ ] 배포 (Railway/Render)
- [ ] README 작성

### Phase 2: 베타 테스트 (2월)
- [ ] 외부 사용자 피드백 수집
- [ ] 버그 수정
- [ ] UX 개선

### Phase 3: 수익화 (추후)
- [ ] 카카오/구글 로그인
- [ ] 프리미엄 구독 기능
- [ ] 결제 연동

---

## 7. API 명세

### 7.1 구독 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /subscriptions | 구독 목록 조회 |
| POST | /subscriptions | 구독 등록 |
| GET | /subscriptions/{id} | 구독 상세 |
| PUT | /subscriptions/{id} | 구독 수정 |
| DELETE | /subscriptions/{id} | 구독 삭제 |

### 7.2 출석 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /subscriptions/{id}/check-in | 출석 체크 |
| DELETE | /usage/{id} | 출석 취소 |

### 7.3 대시보드 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /dashboard | 대시보드 데이터 |
| GET | /calendar?year=&month= | 캘린더 데이터 |

---

## 8. 비기능 요구사항

### 8.1 성능
- 페이지 로드: 2초 이내
- 출석 체크 응답: 500ms 이내

### 8.2 보안
- HTTPS 적용
- UUID는 예측 불가능한 랜덤값

### 8.3 가용성
- 베타 기간 99% 가용성 목표

---

## 9. 제약사항 및 가정

### 9.1 제약사항
- 로그인 없음 (MVP 단계)
- 쿠키 삭제 시 데이터 접근 불가
- 다른 기기에서 동기화 불가

### 9.2 가정
- 사용자는 동일 브라우저에서 접속
- 베타 기간 1개월 운영

---

## 10. 용어 정의

| 용어 | 정의 |
|------|------|
| 일일비용 | 월 환산 금액 ÷ 이번 달 사용 횟수 |
| 월 환산 금액 | 장기 구독(3개월, 12개월 등)을 월 단위로 환산한 금액 |
| 이모지 코드 | DB에 저장되는 이모지 식별자 (gym → 🏋️) |
| 출석 | 구독 서비스를 실제로 사용한 기록 |

---

## 11. 참고 문서

- [PROJECT_SPEC.md](./PROJECT_SPEC.md) - 기술 상세 명세
- [wireframe-v4-calendar.html](./wireframe-v4-calendar.html) - UI 와이어프레임
