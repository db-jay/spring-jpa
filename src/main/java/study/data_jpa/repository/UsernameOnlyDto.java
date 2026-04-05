package study.data_jpa.repository;

public class UsernameOnlyDto {
    private final String username;

    // 클래스 기반 projection은 인터페이스 프록시가 아니라 생성자 호출로 값을 채운다.
    // 그래서 파라미터 이름/순서가 select 절에서 꺼낸 값과 맞아야 한다.
    public UsernameOnlyDto(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
}
