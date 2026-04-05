# Spring Data JPA 학습 프로젝트

Spring JPA와 Spring Data JPA를 단계적으로 비교하며 학습한 프로젝트입니다.  
단순 CRUD 사용법만 보는 것이 아니라, **엔티티 매핑 → 순수 JPA Repository → Spring Data JPA 자동화 → 실무 기능**까지 흐름으로 익히는 데 초점을 두었습니다.

## 학습 목표

- JPA가 엔티티를 어떻게 관리하는지 이해한다.
- 순수 JPA와 Spring Data JPA의 차이를 비교한다.
- 조회, 페이징, 벌크 연산, 연관관계 로딩 전략 같은 핵심 기능을 테스트로 검증한다.
- 실무에서 자주 나오는 projection, auditing, native query, custom repository 패턴을 익힌다.

## 학습 내용

### 1. 엔티티와 기본 JPA 매핑
- `Member`, `Team`, `BaseEntity`, `Item` 엔티티 설계
- `@Entity`, `@Id`, `@GeneratedValue`, `@Column`
- `@MappedSuperclass`로 공통 컬럼 상속
- Auditing 기반 생성/수정 시간, 생성자/수정자 관리
- soft delete 플래그와 공통 베이스 엔티티 구성

### 2. 순수 JPA Repository
- `MemberJpaRepository`, `TeamJpaRepository`를 직접 구현
- `EntityManager`로 저장, 조회, 수정, 삭제 처리
- dirty checking이 어떻게 동작하는지 확인
- Spring Data JPA가 줄여주는 반복 코드가 무엇인지 비교

### 3. Spring Data JPA 기본 기능
- `JpaRepository` 상속만으로 CRUD 자동 제공
- 메서드 이름 기반 쿼리(Derived Query)
- `@Query`를 사용한 JPQL 직접 작성
- 단순 값 조회, DTO 직접 조회, 컬렉션 파라미터 바인딩
- 반환 타입 차이: `List`, 단건, `Optional`

### 4. 페이징과 반환 타입 전략
- `Page`, `Slice`, `List` 차이 학습
- count 쿼리가 언제 필요한지 확인
- `Page.map()`으로 DTO 변환
- 페이징과 fetch join/count 쿼리의 관계 이해

### 5. 벌크 연산과 영속성 컨텍스트
- 벌크 update가 dirty checking 없이 DB에 직접 반영된다는 점 확인
- 벌크 연산 후 영속성 컨텍스트와 DB 상태가 어긋날 수 있음을 학습
- `clearAutomatically = true`로 정합성 맞추는 이유 이해

### 6. 연관관계와 로딩 전략
- 다대일 연관관계의 주인 개념
- `mappedBy`가 외래 키를 바꾸지 않는 이유
- 지연 로딩(LAZY)과 실제 SQL 실행 시점 확인
- fetch join과 `@EntityGraph`를 통한 연관 엔티티 조회 최적화
- cascade, orphan removal로 자식 생명주기 관리

### 7. 조회 최적화 및 부가 기능
- `@QueryHints`로 read-only 조회 힌트 사용
- `@Lock`으로 비관적 락 적용
- 사용자 정의 Repository 확장(`MemberRepositoryCustom`, `Impl`)
- Spring Data Auditing 이벤트 기반 공통 필드 자동 관리

### 8. Projection
- 인터페이스 기반 projection
- DTO(클래스) 기반 projection
- 동적 projection (`Class<T>` 파라미터)
- open projection vs closed projection 차이
- nested projection과 join 비용/최적화 한계 이해

### 9. Native Query
- JPQL로 해결하기 어려운 경우 native query 사용
- 인터페이스 projection과 alias 매핑
- `Page` 반환 시 content 쿼리와 count 쿼리 분리
- native query는 기본 수단이 아니라, **DB 전용 기능/튜닝이 필요한 경우에 선택적으로 사용**한다는 점 학습

### 10. 식별자 전략과 Persistable
- `Persistable` 구현으로 `isNew()` 기준 제어
- 식별자를 직접 할당하는 엔티티에서 persist/merge 분기 이해

## 테스트로 확인한 핵심 주제

- 엔티티 저장/조회/변경 감지
- 메서드 이름 기반 쿼리
- JPQL 직접 작성
- DTO 조회
- 페이징/슬라이스
- 벌크 연산
- fetch join / entity graph
- query hint / lock
- auditing
- projection / native query
- 연관관계 주인 / lazy loading / cascade / orphan removal

## 프로젝트 구조

```text
src/
├── main/java/study/data_jpa/
│   ├── entity/            # Member, Team, BaseEntity, Item
│   ├── repository/        # 순수 JPA + Spring Data JPA Repository
│   ├── dto/               # 조회용 DTO
│   └── controller/        # 간단한 조회 확인용 컨트롤러
└── test/java/study/data_jpa/
    ├── repository/        # Repository 기능 테스트
    ├── relationship/      # 연관관계 SQL 학습 테스트
    └── entity/            # 엔티티 기본 동작 테스트
```

## 한 줄 정리

이 프로젝트는 **JPA의 동작 원리를 먼저 이해한 뒤, Spring Data JPA가 그 위에서 어떤 생산성과 편의 기능을 제공하는지 테스트 중심으로 학습한 프로젝트**입니다.
