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
    
    // @Query(name = ...) 는 JPQL 문자열을 여기 직접 적지 않고
    // 엔티티에 정의한 NamedQuery를 찾아 사용한다.
    // 즉, 메서드 이름 기반 조회와 명시적 쿼리(named query)를 둘 다 학습하는 예시다.
    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);
}
