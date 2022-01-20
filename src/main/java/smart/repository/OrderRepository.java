package smart.repository;

import smart.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    /**
     * 根据订单号返回订单
     *
     * @param no 订单号
     * @return 订单entity
     */
    @Query(value = "select * from t_order where no = :no", nativeQuery = true)
    OrderEntity findByNo(long no);

    /**
     * 根据订单号返回订单(有锁)
     *
     * @param no 订单号
     * @return 订单entity
     */
    @Query(value = "select * from t_order where no = :no for update", nativeQuery = true)
    OrderEntity findByNoForUpdate(@Param("no") long no);
}
