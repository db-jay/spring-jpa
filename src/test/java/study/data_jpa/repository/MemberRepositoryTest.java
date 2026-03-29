package study.data_jpa.repository;

import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.data_jpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
// Spring Data JPA Repository를 테스트한다.
// 바로 위의 MemberJpaJepositoryTest와 비교하면서
// "구현 클래스 없이 인터페이스만으로 어디까지 자동화되는가"를 확인하는 학습용 테스트다.
// 이 클래스는 테스트끼리 서로 영향을 주지 않도록 기본 rollback 동작을 유지한다.
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member saveMember = memberRepository.save(member);

        // findById는 Optional을 반환한다.
        // 순수 JPA의 em.find() 는 null을 줄 수 있지만,
        // Spring Data JPA는 값 없음 상황을 Optional로 더 명시적으로 드러낸다.
        Member findMember = memberRepository.findById(saveMember.getId()).get();
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member);

    }

    @Test
    public void basicCRUD() {
        // JpaRepository 하나만 상속해도 기본 CRUD 메서드가 이미 제공된다.
        // 이 테스트는 순수 JPA 버전과 동일한 검증을 하면서
        // "직접 구현 코드가 사라진다"는 점을 비교하는 역할을 한다.
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

//        단건 조회 검증
        Member findMemberA = memberRepository.findById(member1.getId()).get();
        Member findMemberB = memberRepository.findById(member2.getId()).get();
        assertThat(findMemberA).isEqualTo(member1);
        assertThat(findMemberB).isEqualTo(member2);

//        리스트 조회 검증
        List<Member> findAll = memberRepository.findAll();
        assertThat(findAll.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        count = memberRepository.count();
        assertThat(count).isEqualTo(0);
    }




    @Test
    public void findByUsernameAndAgeGreaterThan() {
        // 메서드 이름 기반 쿼리(Derived Query) 학습용 테스트다.
        // Repository 구현 없이 메서드 이름만으로
        // username = ? and age > ? 조건이 자동으로 해석되는지 확인한다.
        Member aaa = new Member("AAA", 20);
        Member bbb = new Member("BBB", 10);
        memberRepository.save(aaa);
        memberRepository.save(bbb);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        // 조건에 맞는 회원이 하나만 조회되어야 한다.
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }
}
