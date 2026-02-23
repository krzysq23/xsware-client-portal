package pl.xsware.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import pl.xsware.infrastructure.storage.StorageProperties;

@Configuration
@EnableConfigurationProperties(value = StorageProperties.class)
public class StorageConfig {
}
