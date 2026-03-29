package study.data_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.data_jpa.entity.Member;

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
}
