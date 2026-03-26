package study.data_jpa.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
// 학습 중 SQL 결과를 직접 확인하기 위해 rollback을 끄고 본다.
@Rollback(false)
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Test

    public void testEntity() {
        // Team이 먼저 저장되어 있어야 Member가 team_id 외래키를 가질 수 있다.
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        // Member 생성 시 changeTeam()이 호출되어 양방향 연관관계도 함께 맞춰진다.
        Member member1 = new Member("memberA", 10, teamA);
        Member member2 = new Member("memberA", 20, teamA);
        Member member3 = new Member("memberA", 30, teamB);
        Member member4 = new Member("memberA", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // flush: 영속성 컨텍스트의 변경 내용을 DB에 반영
        // clear: 1차 캐시를 비워서 이후 조회가 실제 SQL로 나가게 만듦
        em.flush();
        em.clear();

        // JPQL은 테이블이 아니라 엔티티(Member)를 대상으로 조회한다.
        List<Member> Member = (List<Member>) em.createQuery("select m from Member m", Member.class)
                .getResultList();

        // member.getTeam() 시점에 Team은 지연 로딩으로 조회될 수 있다.
        for (Member member : Member) {
            System.out.println("member = " + member);
            System.out.println(" -> member.team = " + member.getTeam());
        }
    }

}
