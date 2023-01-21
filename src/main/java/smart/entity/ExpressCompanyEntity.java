package smart.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_express_company")
public class ExpressCompanyEntity extends BaseEntity {
    @Id
    private long id;
    private String name;
    private String url;
    private long recommend;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRecommend() {
        return recommend;
    }

    public void setRecommend(long recommend) {
        this.recommend = recommend;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
