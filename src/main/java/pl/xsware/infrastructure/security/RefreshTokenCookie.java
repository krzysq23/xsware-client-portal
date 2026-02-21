package pl.xsware.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import pl.xsware.infrastructure.common.SecurityConstants;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookie {

    private final JwtProperties props;

    public ResponseCookie create(String refreshToken) {
        Duration ttl = props.refreshTokenTtl();
        return ResponseCookie.from(SecurityConstants.REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(ttl)
                .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(SecurityConstants.REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}