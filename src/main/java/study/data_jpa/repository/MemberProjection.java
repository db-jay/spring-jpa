package study.data_jpa.repository;

public interface MemberProjection {

    Long getId();
    String getUsername();
    // native query projection은 컬럼명 자체보다 alias -> getter 이름 매핑이 더 중요하다.
    String getTeamName();
}
