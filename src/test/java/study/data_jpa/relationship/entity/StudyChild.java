package study.data_jpa.relationship.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_child")
public class StudyChild {

    @Id
    @GeneratedValue
    @Column(name = "study_child_id")
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_parent_id")
    private StudyParent parent;

    protected StudyChild() {
    }

    public StudyChild(String name) {
        this.name = name;
    }

    void assignParent(StudyParent parent) {
        this.parent = parent;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public StudyParent getParent() {
        return parent;
    }
}
