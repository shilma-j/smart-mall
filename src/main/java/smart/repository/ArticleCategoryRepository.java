package smart.repository;

import smart.entity.ArticleCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleCategoryRepository extends JpaRepository<ArticleCategoryEntity, Long> {

    @Query(value = "select * from t_article_category order by recommend desc", nativeQuery = true)
    List<ArticleCategoryEntity> findAllByOrderByRecommendDesc();
}
