package smart.authentication;

import smart.config.RedisConfig;
import smart.entity.UserEntity;
import smart.util.Helper;
import smart.lib.Json;
import smart.lib.session.Session;

import java.util.Set;

public class UserToken extends Token {
    public final static String REDIS_USER_PREFIX = "user:";

    public UserToken() {
    }

    public UserToken(UserEntity userEntity) {
        super(userEntity);
    }


    public static void deleteToken(long uid, String redisKey) {
        RedisConfig.getStringRedisTemplate().delete(redisKey);
        RedisConfig.getStringRedisTemplate().opsForSet().remove(REDIS_USER_PREFIX + uid, redisKey);
    }

    /**
     * @param session user session
     * @return user token
     */
    public static UserToken from(Session session) {
        if (session == null || session.getId(false) == null) {
            return null;
        }
        String redisKey = Session.REDIS_PREFIX + session.getId(false);
        Object obj = session.get(Helper.camelCase(UserToken.class));
        if (obj instanceof String) {
            UserToken userToken = UserToken.from((String) obj);
            if (userToken == null) {
                return null;
            }
            Boolean exists = RedisConfig.getStringRedisTemplate().opsForSet().isMember(REDIS_USER_PREFIX + userToken.getId(), redisKey);
            if (exists == null || !exists) {
                session.destroy();
                return null;
            }
            return userToken;
        }
        return null;
    }

    /**
     * get toke by token, for api
     *
     * @param json String
     * @return user token
     */
    public static UserToken from(String json) {
        var userToken = Json.decode(json, UserToken.class);
        if (userToken != null && userToken.isEffective()) {
            return userToken;
        }

        return null;
    }


    /**
     * save token, for http request
     *
     * @param session session
     */
    public void save(Session session) {
        String redisKey = Session.REDIS_PREFIX + session.getId(true);
        session.set(Helper.camelCase(UserToken.class), toString());
        RedisConfig.getStringRedisTemplate().opsForSet().add(REDIS_USER_PREFIX + getId(), redisKey);
        updateRedis(redisKey);
    }

    /**
     * save and get token, for api
     *
     * @return token
     */
    public String saveAndGetToken() {
        return null;
    }

    /**
     * update redis data
     *
     * @param excludeRedisKey 排除的redis key
     */
    @SuppressWarnings("EmptyCatchBlock")
    private void updateRedis(String excludeRedisKey) {
        Set<String> set = RedisConfig.getStringRedisTemplate().opsForSet().members(REDIS_USER_PREFIX + getId());
        assert set != null;
        set.forEach(redisKey -> {
            if (redisKey.equals(excludeRedisKey)) {
                return;
            }
            UserToken userToken = null;
            Object obj = RedisConfig.getStringObjectRedisTemplate().opsForHash().get(redisKey, Helper.camelCase(UserToken.class));
            if (obj instanceof String) {
                userToken = UserToken.from((String) obj);
            }
            if (userToken == null) {
                deleteToken(getId(), redisKey);
                return;
            }
            if (userToken.getId().equals(getId()) && userToken.getName().equals(getName()) && userToken.getPassword().equals(getPassword()) && userToken.getSalt().equals(getSalt())) {
                if (!userToken.getAvatar().equals(getAvatar()) || !userToken.getPhone().equals(getPhone()) || userToken.getLevel() != getLevel()) {
                    userToken.setAvatar(getAvatar());
                    userToken.setLevel(getLevel());
                    userToken.setPhone(getPhone());
                    RedisConfig.getStringObjectRedisTemplate().opsForHash().put(redisKey, Helper.camelCase(UserToken.class), toString());
                }
            } else {
                deleteToken(getId(), redisKey);
            }

        });
    }

    @Override
    public String toString() {
        return Json.encode(this);
    }
}
