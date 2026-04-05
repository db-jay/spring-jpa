package study.data_jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
// 식별자를 애플리케이션에서 직접 넣는 엔티티는 id가 null이 아니어도 "신규"일 수 있다.
// Persistable을 구현하면 Spring Data JPA의 기본 신규 판단(id null 여부) 대신
// isNew() 기준을 우리가 직접 정해 persist / merge 분기를 학습할 수 있다.
public class Item implements Persistable<String> {

    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdAt;

    public Item(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        // 저장 전에는 Auditing이 아직 createdAt을 채우지 않았으므로 신규 엔티티로 본다.
        // 직접 할당 id("itemA" 같은 값)가 이미 있어도 처음 save()에서는 persist 쪽으로 유도한다.
        return createdAt == null;
    }
}
