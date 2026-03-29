package study.data_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;

import java.util.Collection;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
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
}
