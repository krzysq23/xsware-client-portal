package pl.xsware.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
        Provider provider,
        Local local,
        S3 s3,
        Avatar avatar
) {

    public enum Provider { local, s3 }

    public record Local(String baseDir) {}
    public record S3(String bucket, String prefix, String region) {}
    public record Avatar(long maxBytes, List<String> allowedContentTypes) {}
}