package smart.service;

import jakarta.annotation.Resource;
import smart.config.AppConfig;
import smart.entity.UserAddressEntity;
import smart.util.DbUtils;
import smart.repository.UserAddressRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserAddressService {

    @Resource
    UserAddressRepository userAddressRepository;

    @Transactional
    public String addAddress(UserAddressEntity entity) {
        long maxAddress = 10;
        if (userAddressRepository.countByUserId(entity.getUserId()) >= maxAddress) {
            return "新增地址失败，现有地址已达到上限(" + maxAddress + "条)";
        }
        String sql = "insert into t_user_address (user_id, consignee,phone,region,address,dft) values (?,?,?,?,?,?)";
        AppConfig.getJdbcTemplate().update(sql,
                entity.getUserId(), entity.getConsignee(), entity.getPhone(),
                entity.getRegion(), entity.getAddress(), entity.getDft());
        entity.setId(DbUtils.getLastInsertId());
        if (entity.getDft() > 0) {
            AppConfig.getJdbcTemplate().update("update t_user_address set dft=0 where user_id=? and id != ?",
                    entity.getUserId(), entity.getId());
        }
        return null;
    }

    @Transactional
    public void setDefault(UserAddressEntity entity) {
        AppConfig.getJdbcTemplate().update("update t_user_address set dft=1 where id=?", entity.getId());
        AppConfig.getJdbcTemplate().update("update t_user_address set dft=0 where user_id=? and id != ?",
                entity.getUserId(), entity.getId());
    }

    @Transactional
    public void updateAddress(UserAddressEntity entity) {
        String sql = "update t_user_address set user_id=?,consignee=?,phone=?,region=?,address=?,dft=? where id=?";
        AppConfig.getJdbcTemplate().update(sql,
                entity.getUserId(), entity.getConsignee(), entity.getPhone(),
                entity.getRegion(), entity.getAddress(), entity.getDft(), entity.getId());
        if (entity.getDft() > 0) {
            AppConfig.getJdbcTemplate().update("update t_user_address set dft=0 where user_id=? and id != ?",
                    entity.getUserId(), entity.getId());
        }
    }
}
