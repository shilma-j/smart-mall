package smart.config;

import smart.cache.SystemCache;
import smart.storage.LocalStorage;
import smart.storage.OssStorage;
import smart.storage.Storage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@DependsOn("SystemCache")
public class StorageConfig {
    private String storageType;

    StorageConfig() {
        storageType = SystemCache.getStorageType();
        if (!Set.of("local", "oss").contains(storageType)) {
            storageType = "local";
        }
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Storage getStorage() {
        if (storageType.equals("oss")) {
            return new OssStorage();
        } else {
            return new LocalStorage();
        }
    }
}
