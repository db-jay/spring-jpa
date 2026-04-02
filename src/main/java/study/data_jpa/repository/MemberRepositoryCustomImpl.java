package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import study.data_jpa.entity.Member;

import java.util.List;

@RequiredArgsConstructor
// 구현체 이름을 "기본 Repository 인터페이스 이름 + Impl" 로 맞추면
// Spring Data JPA가 이 클래스를 사용자 정의 구현으로 자동 연결한다.
// 여기서는 MemberRepository + Impl 이므로 MemberRepository의 확장 구현으로 인식된다.
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom{

    private final EntityManager em;

    // 복잡한 동적 쿼리, Querydsl, 다단계 로직처럼
    // "파생 쿼리 메서드/간단한 @Query"를 넘어서는 경우 이 영역으로 빼는 패턴을 자주 쓴다.
    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m")
                .getResultList();
    }
}
