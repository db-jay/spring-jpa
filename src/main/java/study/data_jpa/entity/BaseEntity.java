package study.data_jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// @MappedSuperclass 는 이 클래스가 테이블로 생성되는 엔티티가 아니라
// 공통 매핑 정보(createdAt, updatedAt 등)를 자식 엔티티에게 상속하는 용도임을 뜻한다.
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity {

    // insert 시점에 한 번만 채워지고 이후 update 대상에서는 제외된다.
    @CreatedDate
    @Column(updatable = false)
    public LocalDateTime createdAt;

    // 엔티티가 변경되어 update 될 때 마지막 수정 시각이 갱신된다.
    @LastModifiedDate
    public LocalDateTime updatedAt;

    // 누가 처음 생성했는지 기록한다.
    @CreatedBy
    @Column(updatable = false)
    public String createdBy;

    // 마지막 수정한 사용자를 기록한다.
    @LastModifiedBy
    public String updatedBy;

    // soft delete 용 플래그다.
    // 실제 row를 바로 지우지 않고 "삭제된 데이터"로 표시만 해 두면
    // 복구, 감사 추적, 운영 이력 확인이 쉬워진다.
    @Column(name = "is_deleted", nullable = false)
    public boolean deleted = false;

    // delete() SQL을 바로 날리는 대신 상태만 바꾸는 soft delete 예제다.
    public void markDeleted() {
        this.deleted = true;
    }

    public void restore() {
        this.deleted = false;
    }
}
