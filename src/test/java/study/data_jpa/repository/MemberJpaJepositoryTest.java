package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.data_jpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
// 순수 JPA Repository 구현을 테스트한다.
// 목적은 "EntityManager로 직접 구현하면 어떤 반복 코드가 생기는지"를 먼저 체감하고,
// 이후 Spring Data JPA Repository와 비교하는 데 있다.
class MemberJpaJepositoryTest {

    @Autowired MemberJpaJepository memberJpaJepository;
    @PersistenceContext EntityManager em;

    @Test
    public void testMember() {
        // 생성자로 username을 넣어 간단히 엔티티를 만든다.
        Member member = new Member("memberA");
        Member saveMember = memberJpaJepository.save(member);

        // 같은 트랜잭션 안에서는 영속성 컨텍스트(1차 캐시)에서 조회될 수 있다.
        Member findMember = memberJpaJepository.find(saveMember.getId());

        assertThat(findMember.getId()).isEqualTo(saveMember.getId());
        assertThat(findMember.getUsername()).isEqualTo(saveMember.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        // save / find / findAll / count / delete 를 직접 구현해 보면
        // 단순 CRUD에도 반복 메서드가 계속 필요하다는 점을 확인할 수 있다.
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberJpaJepository.save(member1);
        memberJpaJepository.save(member2);

//        단건 조회 검증
        Member findMemberA = memberJpaJepository.findById(member1.getId()).get();
        Member findMemberB = memberJpaJepository.findById(member2.getId()).get();
        assertThat(findMemberA).isEqualTo(member1);
        assertThat(findMemberB).isEqualTo(member2);

//        리스트 조회 검증
        List<Member> findAll = memberJpaJepository.findAll();
        assertThat(findAll.size()).isEqualTo(2);

        long count = memberJpaJepository.count();
        assertThat(count).isEqualTo(2);

        memberJpaJepository.delete(member1);
        memberJpaJepository.delete(member2);

        count = memberJpaJepository.count();
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void dirtyChecking() {
        Member member = new Member("memberA");
        memberJpaJepository.save(member);

        // 같은 트랜잭션 안에서 조회한 엔티티는 영속 상태다.
        // 따라서 setter/비즈니스 메서드로 값만 바꿔도 JPA가 변경을 감지한다.
        Member findMember = memberJpaJepository.find(member.getId());
        findMember.changeUserName("memberAA");

        // flush 시점에 update SQL이 DB로 반영된다.
        // 이 테스트는 commit까지 갈 필요 없이 같은 트랜잭션 안에서 flush/clear 후 다시 읽어
        // 변경 감지를 확인할 수 있으므로 기본 rollback을 유지해도 충분하다.
        em.flush();
        em.clear();

        // clear 이후 native query로 다시 읽으면 1차 캐시가 아니라 실제 DB 값을 확인할 수 있다.
        String username = (String) em.createNativeQuery(
                        "select username from member where member_id = :id")
                .setParameter("id", member.getId())
                .getSingleResult();

        assertThat(username).isEqualTo("memberAA");
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        // 이 테스트는 메서드 이름은 Spring Data JPA 스타일처럼 보이지만,
        // 실제 내부 구현은 MemberJpaJepository 안에서 JPQL을 직접 작성한 버전이다.
        // 즉 "조건 조회를 손으로 구현하면 이렇게 된다"는 기준점 역할을 한다.
        Member aaa = new Member("AAA", 20);
        Member bbb = new Member("BBB", 10);
        memberJpaJepository.save(aaa);
        memberJpaJepository.save(bbb);

        List<Member> result = memberJpaJepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        // username = "AAA" 이면서 age > 15 조건을 만족하는 데이터만 남아야 한다.
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result).hasSize(1);
    }
}
