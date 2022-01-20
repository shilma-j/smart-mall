package smart.repository;

import smart.entity.SpecEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecRepository extends JpaRepository<SpecEntity, Long> {
}
