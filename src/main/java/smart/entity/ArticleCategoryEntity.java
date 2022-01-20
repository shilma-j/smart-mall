package smart.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_article_category")
public class ArticleCategoryEntity extends BaseEntity {

    // 临时变量，存放该分类下的所有文章
    @Transient
    private List<ArticleEntity> articles;

    @Id
    private long id;
    private String name;
    private boolean footerShow;

    private long recommend;


    public List<ArticleEntity> getArticles() {
        return articles;
    }

    public void setArticles(List<ArticleEntity> articles) {
        this.articles = articles;
    }

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

    public boolean isFooterShow() {
        return footerShow;
    }

    public void setFooterShow(boolean footerShow) {
        this.footerShow = footerShow;
    }

    public long getRecommend() {
        return recommend;
    }

    public void setRecommend(long recommend) {
        this.recommend = recommend;
    }
}
