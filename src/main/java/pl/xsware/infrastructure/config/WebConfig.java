package pl.xsware.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pl.xsware.infrastructure.security.CorsProperties;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(value = CorsProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(String.valueOf(corsProperties.origins()))
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                .allowedHeaders(String.valueOf(corsProperties.headers()))
                .maxAge(corsProperties.maxAge())
                .allowCredentials(true);
    }
}