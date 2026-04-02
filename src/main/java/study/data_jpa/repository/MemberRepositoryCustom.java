package study.data_jpa.repository;

import study.data_jpa.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    // 기본 메서드 이름 규칙이나 @Query만으로 표현하기 애매한 로직은
    // 별도 Custom 인터페이스로 분리해 "사용자 정의 확장 포인트"를 만든다.
    List<Member> findMemberCustom();
}
