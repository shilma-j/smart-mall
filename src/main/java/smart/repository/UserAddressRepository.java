package smart.repository;

import smart.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {

    long countByUserId(long uid);

    @Query(value = "select * from t_user_address where user_id = ? order by dft desc", nativeQuery = true)
    List<UserAddressEntity> findAllByUserId(long uid);

    UserAddressEntity findByIdAndUserId(long id, long uid);

}
