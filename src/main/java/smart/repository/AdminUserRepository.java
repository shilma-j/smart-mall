package smart.repository;

import smart.entity.AdminUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUserEntity, Long> {
    @Query(value = "select * from t_admin_user where user_id = ? for update", nativeQuery = true)
    AdminUserEntity findByUserIdForUpdate(long id);
}
