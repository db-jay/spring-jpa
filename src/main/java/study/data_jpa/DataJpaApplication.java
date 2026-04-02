package study.data_jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;
import java.util.UUID;

// @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy 를 자동 채우려면
// 애플리케이션에 Auditing 기능을 켜 두어야 한다.
@EnableJpaAuditing // Auditing 허용
@SpringBootApplication
public class DataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataJpaApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorProvider() {
		// createdBy / updatedBy 에 들어갈 현재 사용자 정보를 제공한다.
		// 실무에서는 로그인 사용자 ID를 넣고, 지금은 학습용으로 임의 UUID를 사용한다.
		return () -> Optional.of(UUID.randomUUID().toString()); // 실무에서는 SpringSecurity등을 사용. 테스트 예시
	}
}
