package smart.repository;

import smart.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "select * from t_user where id = ? for update", nativeQuery = true)
    UserEntity findByIdForUpdate(long id);

    UserEntity findByName(String name);

    @Modifying
    @Query(value = "update t_user set last_login_ip = :lastLoginIp, last_login_time=now() where id = :id", nativeQuery = true)
    void updateForLogin(@Param("id") long id, @Param("lastLoginIp") String ip);

    @Modifying
    @Query(value = "update t_user set password = :password, salt = :salt where id = :id", nativeQuery = true)
    int updateForPassword(@Param("id") long id, @Param("password") String password, @Param("salt") String salt);

    @Modifying
    @Query(value = "update t_user set phone = :phone where id = :id", nativeQuery = true)
    int updateInfo(@Param("id") long id, @Param("phone") String phone);

}
