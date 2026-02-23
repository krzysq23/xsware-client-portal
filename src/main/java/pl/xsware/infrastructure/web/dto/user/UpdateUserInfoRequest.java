package pl.xsware.infrastructure.web.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateUserInfoRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 50) String phone,
        Long version
) {}