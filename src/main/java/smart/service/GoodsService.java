package smart.service;

import org.springframework.stereotype.Service;
import smart.cache.CategoryCache;
import smart.entity.CategoryEntity;
import smart.lib.Helper;
import smart.lib.Pagination;
import smart.repository.GoodsRepository;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class GoodsService {
    @Resource
    private GoodsRepository goodsRepository;

    /**
     * 获取商品列表
     *
     * @param categoryId category id
     * @param keyword    keyword
     * @param sort       sort
     * @param page       page
     */
    public Pagination getGoodsList(long categoryId, String keyword, String sort, long page) {

        StringBuilder sql = new StringBuilder("select id,imgs,name,price from t_goods where status & 0b10 > 0");
        CategoryEntity categoryEntity = CategoryCache.getEntityById(categoryId);
        if (categoryEntity != null) {
            sql.append(" and cate_id in (").append(categoryId);
            for (var item : CategoryCache.getChildren(categoryId)) {
                sql.append(",").append(item.getId());
            }
            sql.append(")");
        } else {
            categoryId = 0;
        }

        if (keyword.length() > 0) {
            keyword = Helper.stringRemove(keyword, "'", "\"", "?", "%", "_", "[", "]").trim();
            StringBuilder qSql = new StringBuilder("'%");
            for (var key : keyword.split(" ")) {
                if (key.length() > 0) {
                    qSql.append(key).append("%");
                }
            }
            qSql.append("'");
            sql.append(" and name like ").append(qSql);
        }
        sql.append(" order by ");

        switch (sort) {
            // new 发布时间排序
            case "n" -> sql.append("update_time desc,recommend desc,id desc");

            // price 价格排序
            case "p1" -> sql.append("price,recommend desc,update_time desc,id desc");

            // 价格倒序
            case "p2" -> sql.append("price desc,recommend desc,update_time desc,id desc");

            // 默认按推荐排序
            default -> {
                sort = "";
                sql.append("recommend desc,update_time desc,id desc");
            }
        }
        Pagination pagination = Pagination.newBuilder(sql.toString())
                .page(page)
                .query(Map.of("cid", Long.toString(categoryId), "q", keyword, "sort", sort))
                .build();
        String imgs;
        for (var row : pagination.getRows()) {
            imgs = (String) row.get("imgs");
            row.put("img", imgs.split(",")[0]);
        }
        return pagination;
    }
}
