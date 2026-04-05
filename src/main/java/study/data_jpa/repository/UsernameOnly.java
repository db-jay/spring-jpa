package study.data_jpa.repository;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOnly {
    // @Value(SpEL)를 붙이는 순간 open projection이 되어
    // 필요한 컬럼만 딱 잘라 조회하기보다 target 엔티티 값을 조합하는 쪽에 가깝다.
    @Value("#{target.username + ' ' + target.age + ' ' + target.team.name}")
    String getUsername();
}
