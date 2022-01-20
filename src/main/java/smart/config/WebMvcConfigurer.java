package smart.config;

import smart.storage.LocalStorage;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfigurer extends WebMvcConfigurationSupport {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new SessionArgumentResolvers());
        resolvers.add(new UserTokenArgumentResolvers());
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HttpInterceptor());
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl
                        .maxAge(10, TimeUnit.MINUTES)
                        .cachePublic())
                .resourceChain(true);

        // upload directory mapping
        Path pathUploadDir = Paths.get(LocalStorage.UPLOAD_DIR);
        if (Files.notExists(pathUploadDir)) {
            try {
                Files.createDirectory(pathUploadDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:" + LocalStorage.UPLOAD_DIR)
                .setCacheControl(CacheControl
                        .maxAge(10, TimeUnit.MINUTES)
                        .cachePublic())
                .resourceChain(true);

    }
}

