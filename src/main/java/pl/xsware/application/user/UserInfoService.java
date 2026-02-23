package pl.xsware.application.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.infrastructure.persistence.user.User;
import pl.xsware.infrastructure.persistence.user.UserInfo;
import pl.xsware.infrastructure.persistence.user.UserInfoRepository;
import pl.xsware.infrastructure.persistence.user.UserRepository;
import pl.xsware.infrastructure.storage.AvatarStorage;
import pl.xsware.infrastructure.storage.StorageProperties;
import pl.xsware.infrastructure.web.dto.user.UpdateUserInfoRequest;
import pl.xsware.infrastructure.web.dto.user.UserInfoResponse;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final AvatarStorage avatarStorage;
    private final StorageProperties storageProperties;

    @Transactional(readOnly = true)
    public UserInfoResponse getInfo(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserInfo info = userInfoRepository.findByUserId(userId).orElse(null);

        if (info == null) {
            return new UserInfoResponse(
                    user.getEmail(),
                    "ROLE_" + user.getRole().name(),
                    null, null, null,
                    null,
                    null
            );
        }

        return new UserInfoResponse(
                user.getEmail(),
                "ROLE_" + user.getRole().name(),
                info.getFirstName(),
                info.getLastName(),
                info.getPhone(),
                info.getAvatarPath(),
                info.getVersion()
        );
    }

    @Transactional
    public UserInfoResponse upsertInfo(long userId, UpdateUserInfoRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserInfo info = userInfoRepository.findByUserId(userId)
                .orElseGet(() -> userInfoRepository.save(UserInfo.create(user)));

        if (req.version() != null && info.getVersion() != null && !req.version().equals(info.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException("Stale version");
        }

        info.updateProfile(req.firstName(), req.lastName(), req.phone());

        return new UserInfoResponse(
                user.getEmail(),
                "ROLE_" + user.getRole().name(),
                info.getFirstName(),
                info.getLastName(),
                info.getPhone(),
                info.getAvatarPath(),
                info.getVersion()
        );
    }

    @Transactional
    public void uploadAvatar(long userId, byte[] bytes, String contentType) {
        validateAvatar(bytes, contentType);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserInfo info = userInfoRepository.findByUserId(userId)
                .orElseGet(() -> userInfoRepository.save(UserInfo.create(user)));

        if (info.getAvatarPath() != null) {
            avatarStorage.delete(info.getAvatarPath());
        }

        String path = avatarStorage.save(userId, bytes, contentType);
        info.setAvatar(path, contentType);
    }

    @Transactional(readOnly = true)
    public AvatarStorage.StoredObject downloadAvatar(long userId) {
        UserInfo info = userInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User info not found"));

        if (info.getAvatarPath() == null) {
            throw new EntityNotFoundException("Avatar not found");
        }

        var obj = avatarStorage.load(info.getAvatarPath());
        return new AvatarStorage.StoredObject(obj.bytes(), info.getAvatarContentType());
    }

    @Transactional
    public void deleteAvatar(long userId) {
        UserInfo info = userInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User info not found"));

        if (info.getAvatarPath() != null) {
            avatarStorage.delete(info.getAvatarPath());
        }
        info.clearAvatar();
    }

    private void validateAvatar(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Empty file");
        }
        if (bytes.length > storageProperties.avatar().maxBytes()) {
            throw new IllegalArgumentException("File too large");
        }
        if (contentType == null || !storageProperties.avatar().allowedContentTypes().contains(contentType)) {
            throw new IllegalArgumentException("Unsupported content type");
        }
    }
}