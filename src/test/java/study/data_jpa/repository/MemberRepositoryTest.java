package study.data_jpa.repository;

import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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

    @Test
    public void returnTypeTest() {
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 10);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // 같은 username 이 여러 건이면 List 로 받는다.
        List<Member> members = memberRepository.findListByUsername("memberA");
        assertThat(members).hasSize(1);

        // 단건 조회가 확실한 경우 Optional 로 받는다.
        Member foundMember = memberRepository.findMemberByUsername("memberA").orElseThrow();
        assertThat(foundMember.getUsername()).isEqualTo("memberA");
    }


    @Test
    public void paging() throws Exception {

        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        /*
            // Page:
            // content 조회 쿼리 + total count 쿼리 둘 다 필요할 때 사용한다.
            // 관리자 화면처럼 "총 몇 건인지", "총 몇 페이지인지"를 보여줘야 하면 Page가 적합하다.
            Page<Member> findByUsername(String name, Pageable pageable);

            // Slice:
            // total count는 모르지만 "다음 페이지가 있는지"만 알면 될 때 사용한다.
            // 무한 스크롤 / 더보기 버튼처럼 성능을 조금 더 아끼고 싶을 때 유리하다.
            Slice<Member> findByUsername(String name, Pageable pageable);

            // List + Pageable:
            // 현재 페이지 데이터만 단순히 잘라서 가져온다.
            // total count, hasNext 같은 부가 정보는 없고 content만 필요할 때 가장 단순하다.
            List<Member> findByUsername(String name, Pageable pageable);

            // List + Sort:
            // 페이지는 필요 없고 정렬만 필요할 때 사용한다.
            List<Member> findByUsername(String name, Sort sort);
        */

        // Spring Data JPA는 페이지 번호를 0부터 시작한다.
        // 아래 설정은 "0페이지에서, 3건을, username 내림차순으로" 가져오라는 뜻이다.
        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        // Page 반환 타입이므로
        // 1) 실제 데이터 조회 쿼리
        // 2) 전체 개수(count) 조회 쿼리
        // 두 개가 나가는지 SQL 로그에서 확인해 보면 좋다.
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        // Page는 map()을 지원해서 페이징 메타데이터(total count, total pages 등)는 유지한 채
        // content만 DTO로 안전하게 바꿀 수 있다.
        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        //then
        List<Member> content = page.getContent(); // 현재 페이지(0페이지)의 실제 데이터 3건
        long totalElements = page.getTotalElements();  // 조건에 맞는 전체 데이터 수

        for (Member member : content) {
            System.out.println("member = " + member);
        }
        System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3); // 현재 페이지에 담긴 데이터 개수
        assertThat(totalElements).isEqualTo(5); // 조건에 맞는 전체 데이터 개수
        assertThat(page.getTotalPages()).isEqualTo(2); // 전체 페이지 수 = ceil(5 / 3)
        assertThat(page.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(page.isFirst()).isTrue(); // 첫 페이지인지
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는지
        assertThat(map.getContent()).hasSize(3); // DTO로 바꿔도 페이지 크기는 유지된다.
    }

/*
    @Test
    public void slice() throws Exception {

        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        // Slice도 Pageable을 받기 때문에 정렬/크기 개념은 Page와 같다.
        // 차이는 "전체 몇 건인지"는 계산하지 않는다는 점이다.
        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        // Slice는 보통 count 쿼리를 생략한다.
        // 대신 다음 페이지 존재 여부를 알기 위해 size보다 1건 더 조회할 수 있다.
        Slice<Member> page = memberRepository.findByAge(age, pageRequest);

        //then
        List<Member> content = page.getContent(); // 현재 조각(slice)에 담긴 데이터

        for (Member member : content) {
            System.out.println("member = " + member);
        }

        assertThat(content.size()).isEqualTo(3); // 현재 slice 데이터 개수
//        assertThat(totalElements).isEqualTo(5); // 전체 개수 -> Slice는 모른다.
//        assertThat(page.getTotalPages()).isEqualTo(2); // 전체 페이지 수 -> Slice는 모른다.
        assertThat(page.getNumber()).isEqualTo(0); // 현재 페이지 번호는 알 수 있다.
        assertThat(page.isFirst()).isTrue(); // 첫 페이지인지 알 수 있다.
        assertThat(page.hasNext()).isTrue(); // 다음 페이지 존재 여부만 알면 된다.
    }
*/

}
