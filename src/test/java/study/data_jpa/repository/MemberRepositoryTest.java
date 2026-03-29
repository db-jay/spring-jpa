package study.data_jpa.repository;

import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;
import study.data_jpa.entity.Team;

import java.util.Arrays;
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
    @Autowired
    TeamRepository teamRepository;

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

    @Test
    public void findUser() {
        // @Query 로 JPQL을 직접 적으면 메서드 이름 규칙을 길게 만들지 않고도
        // 원하는 조건을 명시적으로 표현할 수 있다.
        Member aaa = new Member("AAA", 20);
        Member sameNameDifferentAge = new Member("AAA", 30);
        memberRepository.save(aaa);
        memberRepository.save(sameNameDifferentAge);

        List<Member> result = memberRepository.findUser("AAA", 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }



    @Test
    public void findUsernameList() {
        // 엔티티 전체가 아니라 username만 꺼내도 되는 경우
        // 반환 타입을 List<String> 으로 더 가볍게 가져올 수 있다.
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        Member sameNameDifferentAge = new Member("AAA", 30);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> result = memberRepository.findUsernameList();
        for (String username : result) {
            System.out.println(username);
        }
    }

    @Test
    public void findMemberDto() {
        // TeamRepository도 Spring Data JPA가 만든 빈이므로 주입(@Autowired) 받아서 사용해야 한다.
        // DTO 조회를 확인하려면 team 이름까지 함께 조인되는 데이터가 필요하다.
        Team t1 = new Team("team1");
        teamRepository.save(t1);

        Member memberA = new Member("memberA", 10);
        memberA.changeTeam(t1);
        memberRepository.save(memberA);

        // 엔티티를 조회한 뒤 DTO로 바꾸는 것이 아니라
        // JPQL이 바로 MemberDto를 생성해서 반환하는지 확인한다.
        List<MemberDto> memberDto = memberRepository.findMemberDto();
        assertThat(memberDto).hasSize(1);
        assertThat(memberDto.get(0).getUsername()).isEqualTo("memberA");
        assertThat(memberDto.get(0).getTeamName()).isEqualTo("team1");
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() {
        // in 절 + 컬렉션 파라미터 바인딩 학습용 테스트다.
        // 여러 이름을 한 번에 넘겨 where username in (...) 형태로 조회하는 패턴을 본다.
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 10);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<Member> result = memberRepository.findByNames(Arrays.asList("memberA", "memberB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }
}
