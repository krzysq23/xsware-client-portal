package pl.xsware.infrastructure.web.dto.auth;

public record InfoResponse(
        String email,
        String role
) {}