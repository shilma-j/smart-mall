package smart.repository;

import smart.entity.AdminRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminRoleRepository extends JpaRepository<AdminRoleEntity, Long> {
    @Query(value = "select * from t_admin_role where id = ? for update", nativeQuery = true)
    AdminRoleEntity findByIdForUpdate(long id);
}
