package pl.xsware.infrastructure.storage;

public interface AvatarStorage {

    String save(long userId, byte[] bytes, String contentType);
    StoredObject load(String path);
    void delete(String path);

    record StoredObject(byte[] bytes, String contentType) {}
}