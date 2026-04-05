package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    @PersistenceContext
    EntityManager em;

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


    @Test
    public void bulkUpdate() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        Member member5 = memberRepository.save(new Member("member5", 40));
        //when
        // 벌크 연산은 조회한 엔티티를 수정하는 방식이 아니라
        // 조건에 맞는 row를 DB에서 한 번에 update 하는 방식이다.
        int resultCount = memberRepository.bulkAgePlus(20);
        Member refreshedMember5 = memberRepository.findById(member5.getId()).orElseThrow();

        //then
        assertThat(resultCount).isEqualTo(3);
        assertThat(em.contains(member5)).isFalse(); // clearAutomatically = true 덕분에 기존 엔티티는 detach 된다.
        assertThat(member5.getAge()).isEqualTo(40); // 이미 들고 있던 객체 값은 자동으로 41이 되지 않는다.
        assertThat(refreshedMember5.getAge()).isEqualTo(41); // 다시 조회한 엔티티는 DB에 반영된 값을 읽는다.
    }

    @Test
    public void findMemberLazy() {
        //given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1");
        member1.changeTeam(teamA);
        Member member2 = new Member("member1");
        member2.changeTeam(teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);

        // flush + clear를 해야 "이미 1차 캐시에 있어서 team이 보이는 착시"를 없애고
        // 실제 SQL이 어떻게 나가는지(N+1이 나는지, fetch join/entity graph가 막는지) 관찰할 수 있다.
        em.flush();
        em.clear();

        //when
        // 1) 기본 findAll()을 쓰면 Member 조회 후 team을 접근할 때 추가 select가 발생할 수 있다.
        // 2) @EntityGraph를 붙이면 JPQL을 직접 fetch join으로 바꾸지 않아도 team을 함께 조회한다.
        // 3) 즉 EntityGraph는 "조회 조건을 바꾸는 도구"라기보다 "연관 로딩 전략을 선언하는 도구"에 가깝다.
//        List<Member> members = memberRepository.findAll();


        // JPQL fetch join 버전: 쿼리 자체를 직접 제어하고 싶을 때 사용한다.
//        List<Member> members = memberRepository.fineMemberFetchJoin();


        // @EntityGraph + JPQL 버전: JPQL은 단순하게 두고, team을 함께 가져오라는 힌트만 추가한다.
//        List<Member> members = memberRepository.findMemberEntityGraph();


        // @EntityGraph + 메서드명 기반 쿼리 버전:
        // username 조건은 메서드 이름이 만들고, team fetch 전략은 EntityGraph가 담당한다.
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");


        assertThat(members).hasSize(2);

        for (Member member : members) {
            // 1차캐시에서 Member 데이터 호출
            System.out.println("member = " + member.getUsername());

            // EntityGraph가 제대로 동작했다면 여기서 team 접근 시 추가 select 없이
            // 이미 함께 조회된 team 프록시/엔티티 정보를 사용할 수 있다. (SQL 로그로 확인)
            System.out.println("member.teamClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }

    @Test
    public void queryHint() {
        // given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        // when
        // readOnly 힌트가 붙은 조회이므로
        // 엔티티를 조회해도 스냅샷을 만들지 않아 변경 감지 비용을 줄일 수 있다.
        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("jay");

        // 일반 조회였다면 flush 시 update SQL이 나갈 수 있지만,
        // readOnly 힌트 덕분에 dirty checking 대상이 아니어서 update가 발생하지 않는다.
        em.flush();

        // 핵심: "엔티티를 수정하지 못하게 막는 것"이 아니라
        // "수정해도 변경 감지/쓰기 반영을 하지 않게 최적화"하는 용도다.
    }

    @Test
    public void lock() {
        // given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        // when
        // 비관적 락은 조회하면서 DB row에 lock을 건다.
        // SQL 로그에서 select ... for update 형태가 보이면 lock 힌트가 반영된 것이다.
        List<Member> findMember = memberRepository.findLockByUsername("member1");

        // 핵심: 낙관적 락(@Version)은 "충돌을 나중에 감지"하고,
        // 비관적 락은 "충돌 가능성이 큰 구간을 DB lock으로 먼저 막는다."
    }

    @Test
    public void callCustom() {
        // 호출하는 쪽에서는 JpaRepository 기본 메서드와 동일한 memberRepository 빈을 사용하지만,
        // 실제 실행은 Spring Data가 연결한 MemberRepositoryImpl 쪽 사용자 정의 구현으로 위임된다.
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    public void jpaEventBaseEntity() throws Exception {
        //given
        Member member = new Member("member1");
        // save 시점에 AuditingEntityListener가 동작해 createdAt/createdBy 를 채운다.
        memberRepository.save(member);
        Thread.sleep(100);
        member.setUsername("member2");
        // flush 시 dirty checking 으로 update 가 발생하면 updatedAt/updatedBy 도 함께 갱신된다.
        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findById(member.getId()).get();

        //then
        // 핵심은 "직접 시간/사용자를 넣지 않아도" Auditing 설정만으로 공통 이력이 채워진다는 점이다.
        System.out.println("findMember.createdDate = " + findMember.getCreatedAt());
        System.out.println("findMember.updatedDate = " + findMember.getUpdatedAt());
        System.out.println("findMember.createdBy = " + findMember.getCreatedBy());
        System.out.println("findMember.updatedBy = " + findMember.getUpdatedBy());
    }

    @Test
    public void projections() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        // projection SQL을 눈으로 확인할 때 1차 캐시 영향 없이 DB 조회가 일어나게 비운다.
        em.clear();

        //when
        // DTO projection을 고르면 username 컬럼만 바로 DTO로 매핑되는 흐름을 보기 쉽다.
        List<UsernameOnlyDto> result = memberRepository.findProjectionsByUsername("m1", UsernameOnlyDto.class);

        // 중첩 projection은 문법상 필요한 필드만 선언해도
        // team 연관 객체 접근이 들어가는 순간 join이 필요해 "딱 필요한 컬럼만" 조회한다는 느낌이 약해진다.
/*        List<NestedClosedProjection> result = memberRepository.findProjectionsByUsername("m1", NestedClosedProjection.class);*/

        //then
        for (UsernameOnlyDto usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly.getUsername());
        }

/*
        for (NestedClosedProjection nested : result) {
            String username = nested.getUsername();
            System.out.println("username = " + username);

            String teamName = nested.getTeam().getName();
            System.out.println("teamName = " + teamName);
        }
*/
    }
    @Test
    public void nativeQuery() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        // native query도 영속성 컨텍스트 캐시가 아니라 실제 SQL 결과를 보려면 비우고 확인하는 편이 좋다.
        em.clear();

        //when
        // page size를 실제 데이터보다 작게 줘야 content 조회와 count 조회가 왜 분리되는지 관찰하기 쉽다.
        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0, 1));
        List<MemberProjection> content = result.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection = " + memberProjection.getUsername());
            System.out.println("memberProjection = " + memberProjection.getTeamName());
        }

        assertThat(content).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);

    }
}
