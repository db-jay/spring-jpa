package study.data_jpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

// Spring Data JPA 기본 기능(JpaRepository)에
// 직접 만든 확장 기능(MemberRepositoryCustom)도 함께 합쳐서 사용하는 구조다.
// 즉 클라이언트 입장에서는 "한 repository 빈"처럼 보이지만,
// 내부적으로는 Spring Data 구현 + 사용자 정의 구현이 조합된다.
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    // 메서드 이름만으로 where username = ? and age > ? 쿼리를 만들어 준다.
    // 직접 JPQL을 작성하던 순수 JPA Repository와 비교하면
    // "단순 조회 메서드는 선언만으로 끝난다"는 것이 Spring Data JPA의 핵심 학습 포인트다.
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // Top3, ...By 같은 키워드 조합도 메서드 이름 규칙으로 해석된다.
    // 지금은 호출하지 않아도 "제한 조회(limit)도 이름 규칙으로 표현할 수 있다"는 예시다.
    List<Member> findTop3HelloBy();
    
    // @Query("...") 는 Repository 메서드 위에 JPQL을 직접 적는 방식이다.
    // NamedQuery처럼 엔티티 쪽에 쿼리를 따로 두지 않아도 되므로
    // 간단한 고정 쿼리는 이 방식이 더 직관적이다.
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);


    // 엔티티 전체가 아니라 username 컬럼(정확히는 엔티티 필드)만 바로 조회한다.
    // "필요한 값만 조회하면 반환 타입도 엔티티가 아니라 단순 타입이 될 수 있다"는 점을 보여준다.
    @Query("select m.username from Member m")
    List<String> findUsernameList();

    // select new ... 구문을 사용하면 조회 결과를 엔티티가 아니라 DTO로 바로 받을 수 있다.
    // 화면/응답에 필요한 값만 뽑아낼 때 자주 쓰며,
    // 연관 엔티티 이름(team.name)도 한 번에 DTO로 묶어 올 수 있다.
    @Query("select new study.data_jpa.dto.MemberDto(m.id, m.username, t.name) " + "from Member m join m.team t")
    List<MemberDto> findMemberDto();

    // 컬렉션 파라미터 바인딩 예제다.
    // JPQL의 in 절에 List/Collection 을 넘기면 여러 username 조건을 한 번에 처리할 수 있다.
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);


    List<Member> findListByUsername(String username);     // 다수 Collection
    Optional<Member> findMemberByUsername(String username); // 단수 Optional

    // Pageable이 붙으면 Spring Data JPA가 limit/offset(또는 DB별 동등 문법)을 자동 적용한다.
    // 반환 타입에 따라 동작 차이가 생긴다.
    // 1) Page  : content 조회 + count 쿼리 추가 실행
    // 2) Slice : content 조회만 하고, 다음 페이지가 있는지 보려고 size + 1 건 조회할 수 있다.
    // 3) List  : count 쿼리 없이 content만 조회한다.
    //
    // 주의:
    // @Query를 직접 쓰면 "findByAge" 같은 메서드 이름 조건은 자동으로 붙지 않는다.
    // 즉 age 조건이 필요하면 JPQL에 where m.age = :age 를 직접 적어야 한다.
    //
    // 또 Page + fetch join 조합은 count 쿼리와 충돌할 수 있어
    // 복잡한 쿼리에서는 countQuery를 따로 분리하는 패턴을 자주 사용한다.
    @Query(value ="select m from Member m left join fetch m.team t")
//    @Query(value ="select m from Member m left join m.team t", countQuery = "select count(m.username) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);
//    Slice<Member> findByAge(int age, Pageable pageable);
//    List<Member> findByAge(int age, Pageable pageable);

    // 벌크 update/delete 는 엔티티를 한 건씩 dirty checking 하지 않고
    // DB에 바로 반영된다. 그래서 같은 트랜잭션 안에서 이미 조회해 둔 Member가 있다면
    // 메모리 값과 DB 값이 어긋날 수 있으므로 clearAutomatically = true 로 정합성을 맞춘다.
    @Modifying(clearAutomatically = true) // 벌크 쿼리 직후 영속성 컨텍스트를 비워 다음 조회가 DB 값을 다시 읽게 한다.
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);


    // 단순 JPQL
    @Query("select m from Member m left join fetch m.team")
    List<Member> fineMemberFetchJoin();

    // @EntityGraph는 "어떤 연관관계를 함께 조회할지"만 fetch plan으로 지정한다.
    // JPQL을 직접 fetch join으로 바꾸지 않아도 되므로,
    // 기본 CRUD 메서드(findAll)도 override 해서 즉시 로딩 전략만 덮어쓸 수 있다.
    @Override
    @EntityGraph(attributePaths = "team")
    List<Member> findAll();

    // 여기서는 조회 조건은 그대로 두고(team 조건을 추가하지 않음),
    // member를 조회할 때 team도 함께 읽어오라고 힌트만 준다.
    // 즉 "쿼리 조건"과 "연관관계 로딩 전략"을 분리해서 학습하기 좋은 예제다.
    @EntityGraph(attributePaths = "team")
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();


    // 메서드명 기반 쿼리에서도 @EntityGraph를 함께 쓸 수 있다.
    // 그래서 "조건은 메서드명 파생 쿼리", "연관 엔티티 로딩은 EntityGraph"로 역할을 나눌 수 있다.
    @EntityGraph(attributePaths = "team")
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    // Query Hint는 "이 조회를 어떻게 최적화할지" JPA 구현체에게 힌트를 주는 장치다.
    // 여기서는 Hibernate readOnly 힌트를 써서,
    // 조회한 엔티티를 변경 감지(dirty checking) 대상에서 제외하는 학습 예제다.
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    // @Lock은 조회 시점에 DB lock 전략을 함께 지정한다.
    // PESSIMISTIC_WRITE 는 보통 select ... for update 로 번역되어
    // "지금 읽은 row를 다른 트랜잭션이 함부로 수정하지 못하게" 막는 비관적 락 예제다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

    // 반환 타입(Class<T>)을 파라미터로 넘기면
    // 같은 메서드라도 인터페이스 기반/DTO 기반 projection을 상황에 따라 바꿔가며 재사용할 수 있다.
    <T> List<T> findProjectionsByUsername(@Param("username") String username, Class<T> type);

    // native query는 JPQL이 아니라 DB SQL을 직접 쓰는 "탈출구"다.
    // 복잡한 튜닝/DB 전용 문법이 필요할 때 쓰지만, DB 변경 시 가장 먼저 깨질 수 있는 지점이기도 하다.
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);


    // native query + projection 조합에서는 select alias가 getter 이름과 맞아야 한다.
    // 또 Page 반환 타입이면 content 쿼리와 count 쿼리를 분리해 주는 편이 안전하다.
    @Query(value = "SELECT m.member_id as id, m.username, t.name as teamName " +
            "FROM member m left join team t ON m.team_id = t.team_id",
            countQuery = "SELECT count(*) from member",
            nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);

}
