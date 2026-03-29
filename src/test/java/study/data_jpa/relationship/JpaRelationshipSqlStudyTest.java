package study.data_jpa.relationship;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import study.data_jpa.entity.Member;
import study.data_jpa.entity.Team;
import study.data_jpa.relationship.entity.StudyChild;
import study.data_jpa.relationship.entity.StudyParent;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@EntityScan(basePackageClasses = {Member.class, Team.class, StudyParent.class, StudyChild.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:jpa-relationship-study;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=true",
        "logging.level.root=warn",
        "logging.level.org.hibernate.SQL=debug",
        "logging.level.org.hibernate.orm.jdbc.bind=trace"
})
class JpaRelationshipSqlStudyTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    EntityManagerFactory emf;

    @Test
    @DisplayName("연관관계 주인(@ManyToOne)이 FK를 바꾼다")
    void manyToOneOwnerChangesForeignKey() {
        // 무엇을 봐야 하나?
        // 1) member insert 시점에는 team_id 가 null 로 들어간다.
        // 2) 이후 연관관계의 주인인 Member.team 을 바꾸면 update member set team_id=? SQL 이 나간다.
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member member = new Member("memberA", 10);
        em.persist(member);
        em.flush();

        assertThat(readMemberTeamId(member.getId())).isNull();

        em.clear();

        Member persistedMember = em.find(Member.class, member.getId());
        Team persistedTeam = em.find(Team.class, teamA.getId());
        persistedMember.changeTeam(persistedTeam);

        // 여기 flush 로그에서 update member set team_id=? ... 가 핵심이다.
        em.flush();
        em.clear();

        assertThat(readMemberTeamId(member.getId())).isEqualTo(teamA.getId());
    }

    @Test
    @DisplayName("mappedBy 컬렉션만 바꾸면 FK는 바뀌지 않는다")
    void mappedByCollectionOnlyDoesNotChangeForeignKey() {
        // 무엇을 봐야 하나?
        // Team.members 는 읽기 전용 관점이다.
        // team.getMembers().add(member) 만 해서는 member.team_id update SQL 이 나가지 않는다.
        Team teamA = new Team("teamA");
        Member member = new Member("memberA", 10);

        em.persist(teamA);
        em.persist(member);
        em.flush();
        em.clear();

        Team foundTeam = em.find(Team.class, teamA.getId());
        Member foundMember = em.find(Member.class, member.getId());

        // 연관관계의 주인이 아닌 컬렉션만 변경하는 실험
        foundTeam.getMembers().add(foundMember);

        // flush 로그에서 member.team_id update SQL 이 "없어야" 정상이다.
        em.flush();
        em.clear();

        assertThat(readMemberTeamId(member.getId())).isNull();
    }

    @Test
    @DisplayName("LAZY 설정이면 team 을 사용할 때까지 select 하지 않는다")
    void lazyManyToOneLoadsTeamOnlyWhenAccessed() {
        // 무엇을 봐야 하나?
        // 1) Member 조회 SQL
        // 2) member.getTeam().getName() 을 호출하는 순간 Team 조회 SQL
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member member = new Member("memberA", 10, teamA);
        em.persist(member);
        em.flush();
        em.clear();

        Member foundMember = em.find(Member.class, member.getId());

        assertThat(emf.getPersistenceUnitUtil().isLoaded(foundMember, "team")).isFalse();

        // 이 줄에서 추가 select 가 나가는지 콘솔 로그를 보자.
        String teamName = foundMember.getTeam().getName();

        assertThat(teamName).isEqualTo("teamA");
        assertThat(emf.getPersistenceUnitUtil().isLoaded(foundMember, "team")).isTrue();
    }

    @Test
    @DisplayName("cascade + orphanRemoval 은 부모 기준으로 자식 생명주기를 관리한다")
    void cascadeAndOrphanRemovalManageChildLifecycle() {
        // 무엇을 봐야 하나?
        // 1) em.persist(parent) 만 호출했는데 child insert SQL 도 함께 나가는가?
        // 2) 컬렉션에서 자식을 제거한 뒤 flush 하면 delete SQL 이 나가는가?
        StudyParent parent = new StudyParent("parent");
        StudyChild child1 = new StudyChild("child1");
        StudyChild child2 = new StudyChild("child2");

        parent.addChild(child1);
        parent.addChild(child2);

        // child 를 직접 persist 하지 않아도 cascade = ALL 때문에 함께 저장된다.
        em.persist(parent);
        em.flush();

        assertThat(countRows("study_child")).isEqualTo(2L);

        Long parentId = parent.getId();
        Long removedChildId = child1.getId();

        em.clear();

        StudyParent foundParent = em.find(StudyParent.class, parentId);
        StudyChild childToRemove = foundParent.getChildren().get(0);
        foundParent.removeChild(childToRemove);

        // orphanRemoval = true 이므로 flush 시 delete from study_child ... SQL 을 확인할 수 있다.
        em.flush();
        em.clear();

        assertThat(em.find(StudyChild.class, removedChildId)).isNull();
        assertThat(countRows("study_child")).isEqualTo(1L);
    }

    private Long readMemberTeamId(Long memberId) {
        Object rawValue = em.createNativeQuery("select team_id from member where member_id = :memberId")
                .setParameter("memberId", memberId)
                .getSingleResult();

        if (rawValue == null) {
            return null;
        }
        return ((Number) rawValue).longValue();
    }

    private long countRows(String tableName) {
        return ((Number) em.createNativeQuery("select count(*) from " + tableName)
                .getSingleResult()).longValue();
    }
}
