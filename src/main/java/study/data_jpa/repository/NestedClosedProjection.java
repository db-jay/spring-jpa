package study.data_jpa.repository;

public interface NestedClosedProjection {
    String getUsername();

    // 중첩 projection 문법 자체는 편하지만,
    // team 까지 들어가면 결국 join이 필요해서 루트 엔티티(Member)만 조회할 때보다 가볍지 않을 수 있다.
    TeamInfo getTeam();

    // 팀정보가 중첩된 구조
    interface TeamInfo {
        String getName();
    }
}
