package pl.xsware.infrastructure.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "local")
public class LocalAvatarStorage implements AvatarStorage {

    private final Path baseDir;

    public LocalAvatarStorage(StorageProperties props) {
        this.baseDir = Paths.get(props.local().baseDir()).toAbsolutePath().normalize();
    }

    @Override
    public String save(long userId, byte[] bytes, String contentType) {
        try {
            Files.createDirectories(baseDir.resolve("avatars").resolve(String.valueOf(userId)));
            String ext = guessExt(contentType);
            String filename = UUID.randomUUID() + ext;

            Path relative = Paths.get("avatars", String.valueOf(userId), filename);
            Path fullPath = baseDir.resolve(relative).normalize();

            Files.write(fullPath, bytes, StandardOpenOption.CREATE_NEW);
            return relative.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save avatar", e);
        }
    }

    @Override
    public StoredObject load(String path) {
        try {
            Path fullPath = baseDir.resolve(path).normalize();
            byte[] bytes = Files.readAllBytes(fullPath);

            return new StoredObject(bytes, null);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load avatar", e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path fullPath = baseDir.resolve(path).normalize();
            Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete avatar", e);
        }
    }

    private String guessExt(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}