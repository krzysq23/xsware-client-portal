package pl.xsware.infrastructure.security.auth;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUtils {
    private AuthUtils() {}

    public static long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            throw new InsufficientAuthenticationException("No authentication");
        }
        return Long.parseLong(auth.getDetails().toString());
    }
}