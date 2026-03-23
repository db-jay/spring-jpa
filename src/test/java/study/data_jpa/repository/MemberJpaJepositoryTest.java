package study.data_jpa.repository;

import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.data_jpa.entity.Member;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaJepositoryTest {

    @Autowired MemberJpaJepository memberJpaJepository;

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
    void save() {
    }

    @Test
    void find() {
    }
}
