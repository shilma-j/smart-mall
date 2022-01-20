package smart.scheduled;

import smart.config.AppConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

/**
 * 定时任务
 */
@Component
public class Tasks {
    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * 每天凌晨4点清理遗留的临时文件
     */
    @Scheduled(cron = "0 0 4 * * *")
    @SuppressWarnings("all")
    public void cleanTemplateFiles() {
        long now = Calendar.getInstance().getTimeInMillis();
        Path path = Paths.get(AppConfig.getAppDir() + "work", "Tomcat", "localhost", "ROOT");
        log.info("Clean template directory " + path);
        File file = path.toFile();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (var tmp : files) {
                    tmp.delete();
                }
            }
        }
    }
}
