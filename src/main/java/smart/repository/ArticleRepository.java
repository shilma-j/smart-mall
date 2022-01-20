package smart.repository;

import smart.entity.ArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {

    @Query(value = "select * from t_article order by recommend desc, release_time desc", nativeQuery = true)
    List<ArticleEntity> findAllByOrderByRecommendDesc();
}
