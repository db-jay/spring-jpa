package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.web.bind.annotation.RestController;
import study.data_jpa.entity.Member;


@RestController
public class MemberJpaJepository {

    @PersistenceContext
    private EntityManager em;

    // 순수 JPA 학습용 저장 메서드
    // persist()를 호출해도 바로 insert SQL이 나가는 것이 아니라
    // flush 시점까지 영속성 컨텍스트에 먼저 저장될 수 있다.
    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    // 같은 트랜잭션 안에서는 DB select 대신 1차 캐시에서 조회될 수 있다.
    public Member find(Long id) {
        return em.find(Member.class, id);
    }


}
