package smart.controller.admin.sys;

import smart.config.AppConfig;
import smart.config.RedisConfig;
import smart.lib.Helper;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Controller(value = "admin/sys/info")
@RequestMapping(path = "/admin/sys/info")
@Transactional
public class Info {
    @GetMapping(value = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        StringBuilder gc = new StringBuilder();
        for (var bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (gc.length() > 0) {
                gc.append(", ");
            }
            gc.append(bean.getName());
        }

        Runtime rt = Runtime.getRuntime();
        var runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long upTime = runtimeMXBean.getUptime();
        long days = TimeUnit.MILLISECONDS.toDays(upTime);
        upTime -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(upTime);
        upTime -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(upTime);
        upTime -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(upTime);
        String upTimeStr = "";
        if (days == 1) {
            upTimeStr = "1 day, ";
        } else if (days > 1) {
            upTimeStr = String.format("%d days, ", days);
        }
        upTimeStr += String.format("%02d:%02d:%02d", hours, minutes, seconds);

        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/info/index", request);
        modelAndView.addObject("title", "系统信息");
        modelAndView.addObject("dbVersion",
                AppConfig.getJdbcTemplate().queryForObject("select version()", String.class));
        modelAndView.addObject("dbTxIsolation",
                AppConfig.getJdbcTemplate().queryForObject("SELECT @@transaction_isolation", String.class));

        modelAndView.addObject("javaOsName", System.getProperty("os.name"));
        modelAndView.addObject("javaOsVersion", System.getProperty("os.version"));
        modelAndView.addObject("javaName", runtimeMXBean.getVmName());
        modelAndView.addObject("javaVersion", runtimeMXBean.getVmVersion());
        modelAndView.addObject("javaVendor", runtimeMXBean.getVmVendor());
        modelAndView.addObject("vmInfo", System.getProperty("java.vm.info"));
        modelAndView.addObject("cpuCores", Runtime.getRuntime().availableProcessors());
        modelAndView.addObject("javaGc", gc.toString());
        modelAndView.addObject("applicationDir", AppConfig.getAppDir());
        modelAndView.addObject("springBootVersion", SpringApplication.class.getPackage().getImplementationVersion());

        Properties redisProperties = Objects.requireNonNull(RedisConfig.getStringRedisTemplate().getConnectionFactory()).getConnection().serverCommands().info();
        assert redisProperties != null;
        modelAndView.addObject("redisVersion", redisProperties.getProperty("redis_version"));

        BigDecimal usedMemory = new BigDecimal(redisProperties.getProperty("used_memory"));
        String memory;
        if (usedMemory.intValue() < 1024) {
            memory = usedMemory.toString();
        } else if (usedMemory.intValue() < 1024 * 1024) {
            memory = usedMemory.divide(new BigDecimal(1024), 2, RoundingMode.HALF_UP) + " K";
        } else if (usedMemory.intValue() < 1024 * 1024 * 1024) {
            memory = usedMemory.divide(new BigDecimal(1024 * 1024), 2, RoundingMode.HALF_UP) + " M";
        } else {
            memory = usedMemory.divide(new BigDecimal(1024 * 1024 * 1024), 2, RoundingMode.HALF_UP) + " G";
        }
        memory += "B";
        modelAndView.addObject("redisMemory", memory);
        modelAndView.addObject("freeMemory", rt.freeMemory() / 0x100000 + " MB");
        modelAndView.addObject("maxMemory", rt.maxMemory() / 0x100000 + " MB");
        modelAndView.addObject("totalMemory", rt.totalMemory() / 0x100000 + " MB");
        modelAndView.addObject("startTime", Helper.dateFormat(new Date(runtimeMXBean.getStartTime())));
        modelAndView.addObject("upTime", upTimeStr);

        return modelAndView;
    }
}
