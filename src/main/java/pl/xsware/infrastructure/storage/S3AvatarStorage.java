package pl.xsware.infrastructure.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "s3")
public class S3AvatarStorage implements AvatarStorage {

    private final StorageProperties props;
    private final S3Client s3;

    public S3AvatarStorage(StorageProperties props) {
        this.props = props;
        this.s3 = S3Client.builder()
                .region(Region.of(props.s3().region()))
                .build();
    }

    @Override
    public String save(long userId, byte[] bytes, String contentType) {
        String key = buildKey(userId, contentType);
        s3.putObject(PutObjectRequest.builder()
                        .bucket(props.s3().bucket())
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(bytes));
        return key;
    }

    @Override
    public StoredObject load(String path) {
        GetObjectResponse resp;
        byte[] bytes;
        try (var in = s3.getObject(GetObjectRequest.builder()
                .bucket(props.s3().bucket())
                .key(path)
                .build())) {
            bytes = in.readAllBytes();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load avatar from S3", e);
        }

        return new StoredObject(bytes, null);
    }

    @Override
    public void delete(String path) {
        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(props.s3().bucket())
                .key(path)
                .build());
    }

    private String buildKey(long userId, String contentType) {
        String prefix = props.s3().prefix() == null ? "" : props.s3().prefix();
        if (!prefix.isEmpty() && !prefix.endsWith("/")) prefix += "/";
        String ext = switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> "";
        };
        return prefix + "user/" + userId + "/" + UUID.randomUUID() + ext;
    }
}