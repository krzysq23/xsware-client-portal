package pl.xsware.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.web.cors")
public record CorsProperties(
        List<String> origins,
        List<String> headers,
        long maxAge
) {}