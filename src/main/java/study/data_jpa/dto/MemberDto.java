package study.data_jpa.dto;

import lombok.Data;

@Data
// DTO는 엔티티를 그대로 외부로 노출하지 않고
// "조회에 필요한 값만 묶어서 전달"할 때 사용하는 전형적인 형태다.
// 여기서는 회원 id / username / teamName만 바로 조회하는
// JPQL DTO 프로젝션 예제를 위해 만든다.
public class MemberDto {

    private Long id;
    private String username;
    private String teamName;

    public MemberDto(Long id, String username, String teamName) {
        // JPQL의 select new ... 구문은
        // DTO 생성자 파라미터 순서와 타입이 정확히 맞아야 한다.
        // 그래서 생성자 기반 DTO는 "조회 전용 스냅샷"을 만드는 데 적합하다.
        this.id = id;
        this.username = username;
        this.teamName = teamName;
    }
}
