package smart.repository;

import smart.entity.GoodsSpecEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface GoodsSpecRepository extends JpaRepository<GoodsSpecEntity, Long> {

    long deleteByGoodsIdAndIdNotIn(long goodsId, Collection<Long> notIn);

    List<GoodsSpecEntity> findByGoodsId(long goodsId);

    @Query(value = "select * from t_goods_spec where goods_id = ? order by idx for update", nativeQuery = true)
    List<GoodsSpecEntity> findAllByGoodsIdForUpdate(long goodsId);

    List<GoodsSpecEntity> findAllByGoodsIdOrderByIdxAsc(long goodsId);

    @Query(value = "select * from t_goods_spec where id = ? for update", nativeQuery = true)
    GoodsSpecEntity findByIdForUpdate(long id);
}
