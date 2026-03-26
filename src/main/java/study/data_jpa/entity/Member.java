package study.data_jpa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
// 지금 단계는 "Spring Data JPA가 왜 필요한가?"를 체감하기 전 단계다.
// 먼저 순수 JPA 방식으로 엔티티를 직접 설계하고 값을 바꾸는 메서드도 손수 만들면서,
// 나중에 Spring Data JPA가 어떤 반복 작업을 줄여주는지 비교해 보려는 학습 흐름이다.
// JPA 기본 생성자를 직접 만들지 않고 Lombok으로 protected 생성자를 만든다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member {
    @Id @GeneratedValue
    // DB 컬럼명은 member_id로 두고, 자바 필드명은 id로 단순하게 사용한다.
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    // 지연 로딩으로 설정하면 Member를 조회할 때 Team을 바로 조회하지 않는다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // username만 빠르게 넣어 실습할 때 사용하는 간단 생성자
    // 이런 식으로 객체 생성도 하나씩 직접 다뤄보면서 엔티티 상태 변화를 눈으로 익힌다.
    public Member(String username) {
        this.username = username;
    }

    // 연관관계 실습용 생성자
    // team이 있으면 changeTeam()을 통해 연관관계 편의 메서드를 사용한다.
    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team != null) {
            changeTeam(team);
        }
    }

    // 지금은 update SQL을 직접 작성하지 않고도 값 변경이 반영되는지 배우는 단계다.
    // 그래서 username 변경도 "값만 바꾸면 JPA가 변경 감지한다"는 흐름을 보여주기 위해 메서드로 열어둔다.
    public void changeUserName(String username) {
        this.username = username;
    }

    // 양방향 연관관계에서는 양쪽 값을 함께 맞춰주는 것이 중요하다.
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
