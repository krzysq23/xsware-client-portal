package pl.xsware.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pl.xsware.infrastructure.security.cors.CorsProperties;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsFilterConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties p) {

        var config = new CorsConfiguration();
        config.setAllowedOrigins(p.origins());
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(p.headers());
        config.setAllowCredentials(true);
        config.setMaxAge(p.maxAge());
        config.setExposedHeaders(List.of("Set-Cookie"));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}