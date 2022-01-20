package smart.repository;

import smart.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findAllByOrderByRecommendDesc();

    @Query(value = "select * from t_payment where enable > 0 order by recommend desc for update", nativeQuery = true)
    List<PaymentEntity> findAvailable();

    PaymentEntity findByName(String name);


    @Modifying
    @Query(value = "update t_payment set enable=:enable, config = :config, recommend = :recommend where name = :name", nativeQuery = true)
    @Transactional
    int updateByName(@Param(value = "name") String name,
                     @Param(value = "enable") boolean enable,
                     @Param(value = "recommend") int recommend,
                     @Param(value = "config") String config);
}
