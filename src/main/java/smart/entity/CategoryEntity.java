package smart.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_category")
public class CategoryEntity extends BaseEntity {
    private long i;
    @Id
    private long id;
    private long parentId;
    private String name;
    private long recommend;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getI() {
        return i;
    }

    public void setI(long i) {
        this.i = i;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public long getRecommend() {
        return recommend;
    }

    public void setRecommend(long recommend) {
        this.recommend = recommend;
    }
}