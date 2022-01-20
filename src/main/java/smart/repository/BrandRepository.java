package smart.repository;

import smart.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
    @Query(value = "select * from t_brand where id = ? for update", nativeQuery = true)
    BrandEntity findByIdForUpdate(long id);
}
