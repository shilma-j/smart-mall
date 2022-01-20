package smart.repository;

import smart.entity.ExpressCompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpressCompanyRepository extends JpaRepository<ExpressCompanyEntity, Long> {
    @Query(value = "select * from t_express_company order by recommend desc,id", nativeQuery = true)
    List<ExpressCompanyEntity> findAll();
}
