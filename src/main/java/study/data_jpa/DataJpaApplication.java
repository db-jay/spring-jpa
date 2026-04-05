package study.data_jpa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import study.data_jpa.entity.Member;
import study.data_jpa.repository.MemberRepository;

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

/*	@Bean
	@ConditionalOnProperty(name = "study.seed.enabled", havingValue = "true")
	public CommandLineRunner initData(MemberRepository memberRepository) {
		// 학습용 실행에서는 /members 호출 시 바로 데이터가 보이도록 초기 멤버를 넣어 둔다.
		// 테스트에서는 별도 설정으로 끄고, 로컬 앱 실행에서만 기본값으로 사용한다.
		return args -> {
			for (int i = 0; i < 100; i++) {
				memberRepository.save(new Member("user" + i, i));
			}
		};
	}*/
}
