package study.data_jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter

public class Member {
    @Id @GeneratedValue
    private Long id;
    private String username;

    // JPA는 기본 생성자가 필요하다.
    // public 보다는 protected로 열어두는 것이 의도를 드러내기 좋다.
    protected Member() {
    }

    // 학습용으로 username을 바로 넣어 생성할 수 있게 생성자를 추가했다.
    public Member(String username) {
        this.username = username;
    }

    // 엔티티 값 변경은 메서드로 열어두면 나중에 의도를 파악하기 쉽다.
    public void changeUserName(String username) {
        this.username = username;
    }
}
