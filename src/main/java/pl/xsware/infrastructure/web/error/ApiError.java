package pl.xsware.infrastructure.web.error;

import java.time.Instant;

public record ApiError(
        String code,
        String message,
        Instant timestamp
) {}