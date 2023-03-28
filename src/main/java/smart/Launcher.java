package smart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import smart.cache.GoodsCache;
import smart.config.AppConfig;
import smart.lib.Helper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@DependsOn({"appConfig", "json", "security"})
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Launcher {
    public static void main(String[] args) {
        SpringApplication.run(Launcher.class, args);
        try {
            hot();
        } catch (Exception ignore) {
        }
    }

    /**
     * 启动后，通过调用 自我访问等方式热机
     */
    @SuppressWarnings("unchecked")
    private static void hot() {
        Helper.getQRCodePng("https://hot.test", 100);
        ConfigurableEnvironment environment = AppConfig.getContext().getEnvironment();
        String ip = environment.getProperty("server.address");
        if (ip == null || ip.startsWith("0.")) {
            ip = "127.0.0.1";
        }
        int port = Helper.intValue(environment.getProperty("server.port"));
        if (port == 0) {
            port = 8080;
        }
        Set<String> links = new LinkedHashSet<>();
        links.add("/");
        links.add("/captcha");
        links.add("/cart/json");
        links.add("/favicon.ico");
        links.add("/list");
        for (var recommend : GoodsCache.getRecommend()) {
            List<Map<String, Object>> goodsList = (List<Map<String, Object>>) recommend.get("goodsList");
            if (goodsList.size() > 0) {
                long goodsId = Helper.longValue(goodsList.get(0).get("id"));
                links.add(String.format("/goods/%d.html", goodsId));
                break;
            }
        }
        for (String link : links) {
            sendRequest(String.format("http://%s:%d%s", ip, port, link));
        }
    }

    private static void sendRequest(String uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
        try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
