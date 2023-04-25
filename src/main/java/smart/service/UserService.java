package smart.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import smart.authentication.UserToken;
import smart.entity.UserEntity;
import smart.lib.session.Session;
import smart.lib.status.AccountStatus;
import smart.repository.UserRepository;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.Security;
import smart.util.Validate;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
public class UserService {

    public static final int SALT_LENGTH = 10;

    @Resource
    private UserRepository userRepository;

    public String editPassword(Long uid, String password, String ip) {
        String salt = Helper.randomString(SALT_LENGTH);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(uid);
        userEntity.setPassword(Security.sha3_256(Security.sha3_256(password) + Security.sha3_256(salt)));
        userEntity.setSalt(salt);
        if (DbUtils.update(userEntity, "password", "salt") == 0) {
            return null;
        }
        return salt;
    }

    public Result login(String name, String password, String ip) {
        Result result = new Result();
        String defaultErr = "账号或密码错误";
        if (name != null) {
            name = name.toLowerCase();
        }
        result.error = defaultErr;
        result.errors.put("name", defaultErr);
        result.errors.put("password", defaultErr);
        if (Validate.name(name, "") != null || Validate.password(password, "") != null) {
            return result;
        }
        UserEntity userEntity = userRepository.findByName(name);
        if (userEntity == null) {
            return result;
        }
        long uid = userEntity.getId();
        password = Security.sha3_256(Security.sha3_256(password) + Security.sha3_256(userEntity.getSalt()));
        if (!password.equals(userEntity.getPassword())) {
            return result;
        }
        result.errors.clear();
        long status = userEntity.getStatus();
        if (status != 0) {
            result.errors.put("name", AccountStatus.getStatusInfo(status));
            return result;
        }
        userRepository.updateForLogin(uid, ip);
        result.userEntity = userEntity;
        return result;
    }

    /**
     * 退出登录
     *
     * @param request http request
     */
    public void logout(HttpServletRequest request) {
        Session session = Session.from(request);
        if (session != null) {
            session.delete(Helper.camelCase(UserToken.class));
            request.removeAttribute(Helper.camelCase(UserToken.class));
        }

    }

    /**
     * register user
     *
     * @param name       name
     * @param password   password
     * @param registerIp register ip
     * @return error msg
     */

    public String register(String name, String password, String registerIp) {
        if (DbUtils.first(UserEntity.class, Map.of("name", name)) != null) {
            return "用户名已被注册";
        }
        String salt = Helper.randomString(SALT_LENGTH);
        UserEntity userEntity = new UserEntity();
        userEntity.setName(name);
        userEntity.setPassword(Security.sha3_256(Security.sha3_256(password) + Security.sha3_256(salt)));
        userEntity.setSalt(salt);
        userEntity.setRegisterTime(new Timestamp(System.currentTimeMillis()));
        userEntity.setRegisterIp(registerIp);
        try {
            DbUtils.insert(userEntity, "name", "password", "salt",
                    "registerTime", "registerIp");
        } catch (DuplicateKeyException ex) {
            DbUtils.rollback();
            return "用户名已被注册";
        } catch (Exception ex) {
            DbUtils.rollback();
            ex.printStackTrace();
            return "未知错误,请联系管理员.";
        }
        return null;
    }

    public static class Result {
        public final Map<String, String> errors = new LinkedHashMap<>();
        public String error;
        public UserEntity userEntity;
    }
}
