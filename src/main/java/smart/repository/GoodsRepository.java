package smart.repository;

import smart.entity.GoodsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GoodsRepository extends JpaRepository<GoodsEntity, Long> {
    @Query(value = "select * from t_goods where id = ? for update", nativeQuery = true)
    GoodsEntity findByIdForUpdate(long id);

    long countByCateId(long cateId);
}
