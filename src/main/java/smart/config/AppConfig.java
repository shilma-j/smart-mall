package smart.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import smart.lib.Helper;
import smart.lib.ShutdownThread;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
    // application dir
    private static String appDir;

    private static ConfigurableApplicationContext context;
    private static JdbcTemplate jdbcTemplate;

    // 订单 id
    private static AtomicLong orderId;

    public static String getAppDir() {
        return appDir;
    }

    public static ConfigurableApplicationContext getContext() {
        return context;
    }

    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public static AtomicLong getOrderId() {
        return orderId;
    }


    @Autowired
    private void setConfigurableApplicationContext(ConfigurableApplicationContext context) {
        initAppDir();
        AppConfig.context = context;
        jdbcTemplate = context.getBean(JdbcTemplate.class);
        var id = jdbcTemplate.queryForList("SELECT MAX(id) as id FROM t_order").get(0).get("id");
        AppConfig.orderId = id == null ? new AtomicLong(0L) : new AtomicLong(Helper.longValue(id));
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    /**
     * init application dir
     */
    private void initAppDir() {
        String path = AppConfig.class.getProtectionDomain().getCodeSource().getLocation().toString();
        Pattern pattern = Pattern.compile("^jar:file:(.+?)!");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            String os = System.getProperty("os.name").toLowerCase();
            String dir = matcher.group(1);
            if (os.contains("win") && dir.charAt(0) == '/') {
                dir = dir.substring(1);
            }
            pattern = Pattern.compile("^(.*)([\\\\/])");
            matcher = pattern.matcher(dir);
            if (matcher.find()) {
                if (os.contains("win")) {
                    appDir = matcher.group(1).replace("/", "\\") + "\\";
                }
                appDir = matcher.group(1) + "/";
            } else {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.error(String.format("not found application dir: %s", dir));
                System.exit(-1);
            }

        } else {
            appDir = System.getProperty("user.dir") + File.separator;
        }
        appDir = appDir.intern();
    }

}
