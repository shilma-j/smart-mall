package smart.repository;

import smart.entity.OrderGoodsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderGoodsRepository extends JpaRepository<OrderGoodsEntity, Long> {
    List<OrderGoodsEntity> findAllByOrderNo(long orderNo);

    @Query(value = "select * from t_order_goods where order_no = :orderNo for update", nativeQuery = true)
    List<OrderGoodsEntity> findAllByOrderNoForUpdate(@Param("orderNo") long orderNo);
}
