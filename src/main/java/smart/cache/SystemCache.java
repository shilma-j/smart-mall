package smart.cache;

import smart.lib.Crypto;
import smart.lib.Helper;
import smart.lib.Json;
import smart.repository.SystemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("SystemCache")
@DependsOn("Crypto")
public class SystemCache {
    //备案号
    private static String beian;

    // 首页轮播
    private static String carousel;
    private static List<Map<String, String>> carouselList;

    //静态文件路径
    private static String jsPath;
    //静态文件版本号
    private static String jsVersion;
    private static List<String> keywords;
    private static String keywordsStr;
    private static long maxBuyNum;

    //site name
    private static String siteName;
    private static String storageType;
    private static String ossAk;
    private static String ossAks;
    private static String ossBucket;
    private static String ossBucketUrl;
    private static String ossEndpoint;
    private static String themePc;
    private static String themeMobile;
    //site url
    private static String url;
    private static SystemRepository systemRepository;

    @PostConstruct
    public synchronized static void init() {
        List<String> keywords1 = new ArrayList<>();
        systemRepository.findAll().forEach(entity -> {
            if (entity.getEntity().equals("sys")) {
                switch (entity.getAttribute()) {
                    case "beian" -> beian = entity.getValue();
                    case "carousel" -> setCarousel(entity.getValue());
                    case "jsPath" -> jsPath = entity.getValue();
                    case "jsVersion" -> jsVersion = entity.getValue();
                    case "keywords" -> {
                        keywordsStr = entity.getValue();
                        for (var str : keywordsStr.split(",")) {
                            str = str.trim();
                            if (str.length() > 0) {
                                keywords1.add(str);
                            }
                        }
                        keywords = keywords1;
                    }
                    case "maxBuyNum" -> maxBuyNum = Helper.longValue(entity.getValue());
                    case "siteName" -> siteName = entity.getValue();
                    case "url" -> url = entity.getValue();
                }
                return;
            }
            if (entity.getEntity().equals("storage")) {
                switch (entity.getAttribute()) {
                    case "type" -> storageType = entity.getValue();
                    case "ossAk" -> ossAk = Crypto.decrypt(entity.getValue());
                    case "ossAks" -> ossAks = Crypto.decrypt(entity.getValue());
                    case "ossBucket" -> ossBucket = entity.getValue();
                    case "ossBucketUrl" -> ossBucketUrl = entity.getValue();
                    case "ossEndpoint" -> ossEndpoint = entity.getValue();
                }
                return;
            }
            if (entity.getEntity().equals("theme")) {
                switch (entity.getAttribute()) {
                    case "mobile" -> themeMobile = entity.getValue();
                    case "pc" -> themePc = entity.getValue();
                }
            }
        });
    }

    /**
     * 获取备案号
     *
     * @return 备案号
     */
    public static String getBeian() {
        return beian;
    }

    /**
     * 获取首页轮播JSON
     *
     * @return json string
     */
    public static String getCarousel() {
        return carousel;
    }

    /**
     * 更新首页轮播JSON
     *
     * @param carousel 首页轮播JSON
     */
    public static void setCarousel(String carousel) {
        if (carousel.length() < 2) {
            carousel = "[]";
        }
        SystemCache.carousel = carousel;
        carouselList = Json.toList(carousel);
        if (carouselList == null) {
            carouselList = new ArrayList<>();
        }
    }

    /**
     * 获取首页轮播
     *
     * @return 首页轮播
     */
    public static List<Map<String, String>> getCarouselList() {
        return carouselList;
    }

    public static String getJsPath() {
        return jsPath;
    }

    public static String getJsVersion() {
        return jsVersion;
    }

    public static void setJsVersion(String jsVersion) {
        SystemCache.jsVersion = jsVersion;
    }

    public static List<String> getKeywords() {
        return keywords;
    }

    public static String getKeywordsStr() {
        return keywordsStr;
    }

    public static long getMaxBuyNum() {
        return maxBuyNum;
    }

    public static String getOssAk() {
        return ossAk;
    }

    public static String getOssAks() {
        return ossAks;
    }

    public static String getOssBucket() {
        return ossBucket;
    }

    public static String getOssBucketUrl() {
        return ossBucketUrl;
    }

    public static String getOssEndpoint() {
        return ossEndpoint;
    }

    public static String getSiteName() {
        return siteName;
    }

    public static String getStorageType() {
        return storageType;
    }

    public static String getThemeMobile() {
        return themeMobile;
    }

    public static String getThemePc() {
        return themePc;
    }

    public static String getUrl() {
        return url;
    }

    @Autowired
    private void autowire(SystemRepository systemRepository) {
        SystemCache.systemRepository = systemRepository;
    }
}
