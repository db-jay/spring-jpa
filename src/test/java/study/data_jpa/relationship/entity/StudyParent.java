package study.data_jpa.relationship.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_parent")
public class StudyParent {

    @Id
    @GeneratedValue
    @Column(name = "study_parent_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyChild> children = new ArrayList<>();

    protected StudyParent() {
    }

    public StudyParent(String name) {
        this.name = name;
    }

    public void addChild(StudyChild child) {
        children.add(child);
        child.assignParent(this);
    }

    public void removeChild(StudyChild child) {
        children.remove(child);
        child.assignParent(null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<StudyChild> getChildren() {
        return children;
    }
}
