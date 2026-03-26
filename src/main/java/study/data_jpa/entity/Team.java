package study.data_jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
// Team도 JPA 규칙에 맞게 protected 기본 생성자를 둔다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

    @Id @GeneratedValue
    // DB 컬럼명은 team_id를 사용한다.
    @Column(name = "team_id")
    private Long id;
    private String name;

    // mappedBy = "team" 은 연관관계의 주인이 Member.team 이라는 뜻이다.
    // Team.members 는 읽기 전용으로 관계를 바라보는 쪽이라고 이해하면 된다.
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

    // 팀 이름만 빠르게 넣어 실습할 수 있도록 생성자를 열어둔다.
    public Team(String name) {
        this.name = name;
    }
}
