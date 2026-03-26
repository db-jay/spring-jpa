package study.data_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.data_jpa.entity.Team;

// @Repository 생략 가능, Interface만 봐도 spring이 proxy를 넣어줘야 겠다는 것을 알고 있음.
public interface TeamRepository extends JpaRepository<Team, Long> {
}
