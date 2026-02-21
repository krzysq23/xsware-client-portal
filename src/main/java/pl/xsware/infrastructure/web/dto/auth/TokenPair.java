package pl.xsware.infrastructure.web.dto.auth;

public record TokenPair(
        String accessToken,
        String refreshToken
) {}