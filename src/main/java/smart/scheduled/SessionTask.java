package smart.scheduled;

import smart.config.RedisConfig;
import smart.util.Helper;
import smart.lib.session.ApiSession;
import smart.lib.session.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SessionTask {
    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * 检查 redis key, 异常的直接删除，并返回删除的key数量
     *
     * @param match   配置字符串
     * @param timeout key最大超时
     */
    public void checkKey(String match, long timeout) {
        var redisTemplate = RedisConfig.getStringObjectRedisTemplate();
        redisTemplate.execute((RedisCallback<Void>) conn -> {
            /* 检查清理 http session */
            long ttl;
            String key;
            Cursor<byte[]> cursor = conn.scan(ScanOptions.scanOptions().count(1000).match(match).build());
            while (cursor.hasNext()) {
                key = new String(cursor.next());
                ttl = Helper.longValue(redisTemplate.getExpire(key));
                if (key.length() < 20 || ttl == -1L || ttl > timeout) {
                    redisTemplate.delete(key);
                }
            }
            return null;
        });
    }

    /**
     * 定时检查session/api session数据
     * 每天04:40 运行一次
     */
    @Scheduled(cron = "0 40 4 * * *")
    public void cleanSession() {
        log.info("Clean session data");
        var redisTemplate = RedisConfig.getStringObjectRedisTemplate();
        /* 检查清理 http session */
        checkKey(Session.REDIS_PREFIX + "*", Session.TIMEOUT.toSeconds());
        /* 检查清理 api session */
        checkKey(ApiSession.REDIS_PREFIX + "*", ApiSession.TIMEOUT.toSeconds());

    }
}
