package smart.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import smart.lib.Pagination;
import smart.repository.ArticleRepository;

import java.util.Map;

@Service
public class ArticleService {

    @Resource
    ArticleRepository articleRepository;

    public Pagination getAll(long cateId, long page) {
        String sqlWhere = cateId > 0 ? "where a.cate_id=" + cateId : "";
        String sql = """
                select a.id,
                       a.cate_id,
                       ac.name as cate_name,
                       a.content,
                       a.title,
                       a.release_time as release_time,
                       a.visible,
                       a.recommend
                from t_article a
                         left join t_article_category ac on a.cate_id = ac.id
                %s
                order by a.release_time desc, a.recommend desc
                """;
        sql = String.format(sql, sqlWhere);
        return Pagination.newBuilder(sql)
                .page(page)
                .query(Map.of("cateId", Long.toString(cateId)))
                .build();
    }
}