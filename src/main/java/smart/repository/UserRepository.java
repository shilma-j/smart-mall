package smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smart.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "select * from t_user where id = :id for update", nativeQuery = true)
    UserEntity findByIdForWrite(long id);

    UserEntity findByName(String name);

    @Query(value = "select * from t_user where name = ? for update", nativeQuery = true)
    UserEntity findByNameForWrite(String name);

    @Modifying
    @Query(value = "update t_user set last_login_ip = :lastLoginIp, last_login_time=now() where id = :id", nativeQuery = true)
    void updateForLogin(@Param("id") long id, @Param("lastLoginIp") String ip);

    @Modifying
    @Query(value = "update t_user set password = :password, salt = :salt where id = :id", nativeQuery = true)
    int updateForPassword(@Param("id") long id, @Param("password") String password, @Param("salt") String salt);
}
