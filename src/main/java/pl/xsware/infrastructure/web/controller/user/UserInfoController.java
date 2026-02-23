package pl.xsware.infrastructure.web.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.xsware.application.user.UserInfoService;
import pl.xsware.infrastructure.security.auth.AuthUtils;
import pl.xsware.infrastructure.web.dto.user.UpdateUserInfoRequest;
import pl.xsware.infrastructure.web.dto.user.UserInfoResponse;

import java.io.IOException;

@RestController
@RequestMapping("/api/users/info")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    @GetMapping
    public UserInfoResponse get() {
        long userId = AuthUtils.currentUserId();
        return userInfoService.getInfo(userId);
    }

    @PutMapping
    public UserInfoResponse upsert(@RequestBody @Valid UpdateUserInfoRequest req) {
        long userId = AuthUtils.currentUserId();
        return userInfoService.upsertInfo(userId, req);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAvatar(@RequestPart("file") MultipartFile file) throws IOException {
        long userId = AuthUtils.currentUserId();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        userInfoService.uploadAvatar(userId, file.getBytes(), file.getContentType());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/avatar")
    public ResponseEntity<byte[]> downloadAvatar() {
        long userId = AuthUtils.currentUserId();

        var obj = userInfoService.downloadAvatar(userId);
        if (obj.bytes() == null) return ResponseEntity.notFound().build();

        MediaType mt = (obj.contentType() != null) ? MediaType.parseMediaType(obj.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mt)
                .cacheControl(CacheControl.noStore())
                .body(obj.bytes());
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<Void> deleteAvatar() {
        long userId = AuthUtils.currentUserId();
        userInfoService.deleteAvatar(userId);
        return ResponseEntity.noContent().build();
    }
}