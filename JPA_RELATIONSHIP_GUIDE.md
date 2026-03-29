# JPA 연관관계 학습 가이드

JPA 연관관계는 **문법 암기**보다 먼저 **RDB(관계형 데이터베이스)가 관계를 저장하는 방식**과 **객체가 참조를 가지는 방식**의 차이를 이해해야 잘 보입니다.

이 문서는 아래 3가지를 기준으로 설명합니다.

1. **RDB와 객체는 관계를 표현하는 방식이 다르다**
2. **연관관계의 주인(owning side)은 외래 키(FK)를 관리하는 쪽이다**
3. **실무에서는 `@ManyToOne + LAZY`부터 익히는 것이 가장 안전하다**

---

## 1. RDB의 관계와 객체의 관계는 왜 다를까?

### RDB 관점
- 관계형 DB는 **외래 키(FK)** 로 관계를 저장합니다.
- 예를 들어 `member.team_id -> team.team_id` 이면, 관계는 결국 `member` 테이블 안의 `team_id` 컬럼 하나로 표현됩니다.
- 즉, **DB는 “어느 테이블이 FK를 들고 있느냐”가 관계의 핵심**입니다.

### 객체 관점
- 객체는 외래 키 컬럼이 없습니다.
- 대신 **참조(reference)** 를 가집니다.
- `Member` 객체가 `Team team` 필드를 가지면 `Member -> Team` 방향으로 참조합니다.
- `Team` 객체가 `List<Member> members` 필드를 가지면 `Team -> Member` 방향으로 참조합니다.

### 핵심 차이
- DB 관계: **FK 1개**
- 객체 관계: **참조 1개 또는 2개**

즉, **DB에는 방향이 사실상 하나(FK 기준)** 이지만, **객체는 단방향/양방향 참조를 모두 만들 수 있습니다.**

---

## 2. 단방향 연관관계와 양방향 연관관계

### 2-1. 단방향 연관관계

예: `Member -> Team`

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "team_id")
private Team team;
```

특징:
- `Member`는 자신의 팀을 알 수 있습니다.
- `Team`은 자신에게 속한 회원 목록을 바로 알 수 없습니다.
- **가장 단순하고 실무 친화적**입니다.

언제 좋은가?
- 조회 요구사항이 `회원 -> 팀` 위주일 때
- 처음 연관관계를 학습할 때
- FK를 가진 쪽에서만 관계를 다루고 싶을 때

---

### 2-2. 양방향 연관관계

예:

```java
// Member
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "team_id")
private Team team;

// Team
@OneToMany(mappedBy = "team")
private List<Member> members = new ArrayList<>();
```

특징:
- `Member -> Team`
- `Team -> Members`
- **객체 참조가 양쪽에 모두 존재**합니다.
- 하지만 **DB 관계가 2개가 생기는 것은 아닙니다.**

중요:
- 양방향은 “관계가 2개”가 아니라 **“객체 참조가 2개”** 입니다.
- DB FK는 여전히 `member.team_id` 하나입니다.

---

## 3. 연관관계의 주인: 왜 `Member.team` 이 주인인가?

현재 프로젝트 기준:

```java
// Member
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "team_id")
private Team team;

// Team
@OneToMany(mappedBy = "team")
private List<Member> members = new ArrayList<>();
```

여기서 연관관계의 주인은:

- `Member.team` ✅
- `Team.members` ❌

이유:
- 실제 FK 컬럼 `team_id` 는 `member` 테이블에 있기 때문입니다.
- 즉 **FK를 가진 쪽이 연관관계의 주인**입니다.

### 주인과 비주인의 차이

#### 주인 (`@ManyToOne`, `@JoinColumn`)
- FK 값을 변경할 수 있습니다.
- `member.setTeam(team)` 또는 `member.changeTeam(team)` 하면 DB의 `team_id` 가 바뀝니다.

#### 비주인 (`@OneToMany(mappedBy = "team")`)
- 읽기 전용 관점입니다.
- `team.getMembers().add(member)` 만 해서는 DB FK가 바뀌지 않습니다.
- `mappedBy` 는 “이 필드는 `Member.team` 이 관리하는 관계를 거울처럼 바라만 본다”는 뜻입니다.

---

## 4. 왜 `@ManyToOne` 부터 익혀야 할까?

실무에서 가장 먼저 익혀야 하는 패턴은 아래입니다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "team_id")
private Team team;
```

이유:
- DB FK 구조와 가장 자연스럽게 맞습니다.
- 조회/수정 책임이 명확합니다.
- 성능 문제를 예측하기 쉽습니다.
- 양방향보다 덜 헷갈립니다.

**권장 학습 순서**
1. `@ManyToOne` 단방향
2. 양방향 + `mappedBy`
3. `cascade`, `orphanRemoval`
4. 성능 최적화(fetch join, N+1, 배치 전략)

---

## 5. `@ManyToOne` 주요 옵션과 의미

### 5-1. `fetch`

```java
@ManyToOne(fetch = FetchType.LAZY)
```

#### `FetchType.LAZY` 권장
- 연관 객체를 실제로 사용할 때 SQL을 날립니다.
- `member`만 조회할 때 `team`까지 즉시 가져오지 않습니다.
- 불필요한 조인을 줄이고, N+1 문제를 의식적으로 다루기 좋습니다.

#### `FetchType.EAGER` 주의
- `@ManyToOne` 의 기본값은 `EAGER` 입니다.
- 조회할 때 연관 엔티티를 즉시 같이 읽으려 합니다.
- 화면/로직마다 필요 없는 조인이나 추가 쿼리가 발생할 수 있습니다.
- 실무에서는 **기본값을 믿지 말고 대부분 LAZY로 명시**합니다.

---

### 5-2. `optional`

```java
@ManyToOne(optional = false)
```

- `true`(기본값): 팀이 없는 회원 허용
- `false`: 팀이 반드시 있어야 함

주의:
- 비즈니스적으로 “반드시 부모가 있어야 한다”가 명확할 때만 사용합니다.
- DB에서도 `nullable = false` 와 함께 맞춰주는 편이 안전합니다.

예:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "team_id", nullable = false)
private Team team;
```

---

### 5-3. `cascade`

```java
@ManyToOne(cascade = CascadeType.PERSIST)
```

- 부모 저장 시 자식도 함께 저장하거나,
- merge/remove 등의 영속성 작업을 전파할 수 있습니다.

하지만 `@ManyToOne` 쪽 cascade 는 보통 신중해야 합니다.

이유:
- 다대일의 “일(Team)”은 여러 회원이 공유할 수 있습니다.
- `Member` 저장 시 `Team`까지 함께 저장/삭제하면 생명주기 경계가 애매해질 수 있습니다.

실무 기준:
- `@ManyToOne` 에 cascade 남용 ❌
- **부모가 자식을 완전히 소유하는 집합**일 때 주로 `@OneToMany`/`@OneToOne` 쪽에 씁니다.

---

### 5-4. `@JoinColumn`

```java
@JoinColumn(name = "team_id")
```

의미:
- 어느 컬럼이 FK인지 지정합니다.

주의:
- 컬럼명을 명시하지 않으면 JPA 기본 규칙에 따라 생성되는데,
- 초반 학습에서는 **항상 명시하는 습관**이 이해에 좋습니다.

---

## 6. `@OneToMany` 주요 옵션과 의미

### 6-1. `mappedBy`

```java
@OneToMany(mappedBy = "team")
private List<Member> members = new ArrayList<>();
```

의미:
- 이 컬렉션은 `Member.team` 이 관리하는 관계를 조회만 한다.
- 즉, **연관관계의 주인이 아니다.**

가장 중요한 포인트:
- `mappedBy` 가 붙으면 FK 수정 권한이 없습니다.

---

### 6-2. `cascade`

```java
@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
```

의미:
- 부모 저장/삭제 시 자식에게도 영속성 작업을 전파합니다.

언제 적합한가?
- 자식이 부모에 강하게 소속되고,
- 다른 곳에서 공유되지 않으며,
- 생명주기를 함께 관리해야 할 때

예:
- 주문(`Order`) - 주문상품(`OrderItem`)
- 게시글(`Post`) - 첨부파일(`Attachment`)

주의:
- `Member - Team` 같은 공유 개념에는 함부로 적용하지 않습니다.

---

### 6-3. `orphanRemoval`

```java
@OneToMany(mappedBy = "parent", orphanRemoval = true)
```

의미:
- 부모 컬렉션에서 자식을 제거하면 DB에서도 삭제합니다.

주의:
- **진짜 고아 삭제가 맞는 관계에서만** 사용해야 합니다.
- 다른 aggregate/부모에서 재사용될 수 있는 자식이면 위험합니다.

---

### 6-4. `fetch`

`@OneToMany` 의 기본 fetch 는 `LAZY` 입니다.

이유:
- 컬렉션은 데이터 수가 많아질 수 있으므로 즉시 로딩 비용이 큽니다.
- 기본 LAZY가 상대적으로 안전합니다.

주의:
- 컬렉션 접근 시 추가 쿼리가 나갑니다.
- 화면/목록 조회에서 N+1 문제가 생기기 쉽습니다.

---

## 7. 단방향 / 양방향에서 꼭 주의할 점

### 7-1. 양방향은 양쪽 객체 상태를 같이 맞춰야 한다

현재 프로젝트의 편의 메서드:

```java
public void changeTeam(Team team) {
    this.team = team;
    team.getMembers().add(this);
}
```

왜 필요한가?
- DB 반영은 주인(`Member.team`)이 하지만,
- 메모리 상 객체 그래프도 맞춰야 조회/디버깅 시 안 헷갈립니다.

주의:
- `member.setTeam(team)` 만 하고 `team.getMembers()` 를 안 맞추면
  같은 트랜잭션 안에서 객체 상태가 꼬여 보일 수 있습니다.

---

### 7-2. `Team.members` 만 수정해도 DB FK는 안 바뀐다

아래는 **틀린 예시**입니다.

```java
team.getMembers().add(member); // 이것만으로는 FK가 안 바뀜
```

반드시 주인 쪽도 바꿔야 합니다.

```java
member.changeTeam(team);
```

---

### 7-3. 실무에서는 `toString`, JSON 직렬화 주의

양방향 참조가 있으면:
- `Member -> Team -> Members -> Team -> ...`
- 순환 참조 문제가 생길 수 있습니다.

주의 대상:
- Lombok `@ToString`
- Jackson JSON 직렬화
- 로그 출력

현재 프로젝트도 `@ToString(of = {"id", "username", "age"})` 처럼
연관 필드를 제외하는 방향이 안전합니다.

---

### 7-4. `equals/hashCode` 에 연관관계 넣지 말기

연관관계를 `equals/hashCode` 에 넣으면:
- 순환 참조
- 지연 로딩 강제 초기화
- 영속 상태/비영속 상태 비교 이상

같은 문제가 생길 수 있습니다.

초보 단계에서는:
- 식별자 기반
- 또는 기본 Object 동일성 유지

로 가는 것이 안전합니다.

---

## 8. 성능 관점에서 꼭 알아야 할 것

### 8-1. N+1 문제

예를 들어 회원 100명을 조회한 뒤 각 회원의 팀 이름을 읽으면:
- 회원 조회 1번
- 팀 조회 N번

이 될 수 있습니다.

즉:
- `@ManyToOne(fetch = LAZY)` 자체는 좋은 기본값
- 하지만 목록 조회에서는 **fetch join**, DTO 조회, 배치 전략을 함께 고려해야 합니다.

---

### 8-2. 단방향 `@OneToMany` 는 초반 학습에서 우선순위가 낮다

예:

```java
@OneToMany
@JoinColumn(name = "team_id")
private List<Member> members = new ArrayList<>();
```

이런 구조도 가능하지만 주의가 많습니다.

이유:
- FK는 member 테이블에 있는데,
- Team 쪽에서 관계를 직접 관리하려고 하므로
- insert 후 update가 추가로 나가거나,
- 구조 이해가 더 어려워집니다.

학습 초반에는:
- **`@ManyToOne` 단방향**
- 그 다음 **양방향 + mappedBy**

순서가 훨씬 낫습니다.

---

## 9. 이 프로젝트에서 보면 좋은 테스트 포인트

이번에 추가한 테스트:

- `JpaRelationshipSqlStudyTest.manyToOneOwnerChangesForeignKey`
- `JpaRelationshipSqlStudyTest.mappedByCollectionOnlyDoesNotChangeForeignKey`
- `JpaRelationshipSqlStudyTest.lazyManyToOneLoadsTeamOnlyWhenAccessed`
- `JpaRelationshipSqlStudyTest.cascadeAndOrphanRemovalManageChildLifecycle`

### 특히 로그에서 봐야 하는 것

#### 1) `manyToOneOwnerChangesForeignKey`
- `member` insert 시 `team_id` 가 `null` 로 들어가는지
- 이후 `member.changeTeam(team)` 후 `update member set team_id=? ...` 가 나가는지

#### 2) `mappedByCollectionOnlyDoesNotChangeForeignKey`
- `team.getMembers().add(member)` 만 했을 때
- **`member.team_id` update SQL이 안 나가는지**

#### 3) `lazyManyToOneLoadsTeamOnlyWhenAccessed`
- `Member` 조회 SQL 1번
- `member.getTeam().getName()` 호출 시점에 `Team` 조회 SQL이 추가로 나가는지

#### 4) `cascadeAndOrphanRemovalManageChildLifecycle`
- `em.persist(parent)` 만 했는데 자식 insert 도 함께 나가는지
- 컬렉션에서 자식을 제거한 뒤 flush 시 delete SQL 이 나가는지

---

## 10. 직접 실행해 보는 방법

이 테스트는 **테스트 클래스 안에서 인메모리 H2를 사용하도록 설정**되어 있어서 별도 H2 서버를 띄우지 않아도 됩니다.

```bash
./gradlew test --tests "*JpaRelationshipSqlStudyTest"
```

더 집중해서 보려면:

```bash
./gradlew test --tests "study.data_jpa.relationship.JpaRelationshipSqlStudyTest"
```

로그에서 아래를 중심으로 보세요.

- `insert into team ...`
- `insert into member ...`
- `update member set team_id=? ...`
- `select ... from member ...`
- `select ... from team ...`
- `delete from study_child ...`

---

## 11. 처음 학습할 때의 추천 결론

처음에는 아래만 확실히 가져가면 충분합니다.

1. **DB는 FK로 관계를 저장한다**
2. **JPA에서 FK를 가진 쪽이 연관관계의 주인이다**
3. **양방향은 DB 관계가 2개가 아니라 객체 참조가 2개인 것이다**
4. **`@ManyToOne(fetch = LAZY)` 를 기본 출발점으로 삼는다**
5. **`mappedBy` 쪽은 읽기 전용이며, 주인이 아니면 FK를 바꾸지 못한다**
6. **양방향은 편의 메서드로 양쪽 상태를 같이 맞춘다**
7. **cascade / orphanRemoval 은 생명주기가 완전히 종속될 때만 사용한다**

이 7가지를 이해하면, 이후 JPQL, fetch join, Spring Data JPA, Querydsl 로 넘어갈 때 훨씬 덜 헷갈립니다.
