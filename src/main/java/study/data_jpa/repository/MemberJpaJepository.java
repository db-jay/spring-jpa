package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.web.bind.annotation.RestController;
import study.data_jpa.entity.Member;

import java.util.List;
import java.util.Optional;


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

    public void delete(Member member) {
        em.remove(member);
    }

    // 같은 트랜잭션 안에서는 DB select 대신 1차 캐시에서 조회될 수 있다.
    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    // em.find()는 조회 결과가 없으면 null을 반환한다.
    // null을 그대로 반환하면 호출하는 쪽이 null 체크를 빼먹기 쉽고,
    // 나중에 NullPointerException으로 이어질 수 있다.
    // Optional로 감싸면 "조회 결과가 없을 수도 있다"는 사실이 메서드 반환 타입에 드러난다.
    // 그래서 사용하는 쪽도 orElse(), orElseThrow() 같은 방식으로 값 없음 상황을 더 명시적으로 처리하게 된다.
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public long count() {
        return em.createQuery("select count(m) from Member m", Long.class)
                .getSingleResult();
    }
}
