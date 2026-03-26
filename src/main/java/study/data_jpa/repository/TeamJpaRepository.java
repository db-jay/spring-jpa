package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import study.data_jpa.entity.Team;

import java.util.List;
import java.util.Optional;

@Repository
public class TeamJpaRepository {

    @PersistenceContext
    private EntityManager em;

    public Team save(Team team) {
        em.persist(team);
        return team;
    }

    public void delete(Team team) {
        em.remove(team);
    }

    public List<Team> findAll() {
        return em.createQuery("select t from Team t", Team.class)
                .getResultList();
    }

    // em.find(Team.class, id)를 그대로 반환하면 결과가 없을 때 null이 내려간다.
    // 그런데 findById()는 "있을 수도 있고 없을 수도 있는 조회"라는 의미가 강하므로
    // 반환 타입을 Optional<Team>으로 두는 편이 의도가 더 분명하다.
    // 즉, 단순히 nullable 체크를 위한 것이 아니라
    // "조회 결과 없음"을 API 설계에 명확히 반영하기 위한 선택이다.
    public Optional<Team> findById(Long id) {
        Team team = em.find(Team.class, id);
        return Optional.ofNullable(team);
    }

    public long count() {
        return em.createQuery("select count(t) from Team t", Long.class)
                .getSingleResult();
    }
}
