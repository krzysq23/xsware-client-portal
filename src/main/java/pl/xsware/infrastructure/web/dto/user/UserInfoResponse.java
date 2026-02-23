package pl.xsware.infrastructure.web.dto.user;

public record UserInfoResponse(
        String email,
        String role,
        String firstName,
        String lastName,
        String phone,
        boolean isAvatar,
        Long version
) {}