package pl.xsware.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.domain.user.Role;
import pl.xsware.infrastructure.persistence.auth.RefreshToken;
import pl.xsware.infrastructure.persistence.auth.RefreshTokenRepository;
import pl.xsware.infrastructure.persistence.user.User;
import pl.xsware.infrastructure.persistence.user.UserRepository;
import pl.xsware.infrastructure.security.JwtProperties;
import pl.xsware.infrastructure.security.JwtService;
import pl.xsware.infrastructure.web.dto.auth.*;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProps;
    private final Clock clock;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        var email = req.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        Instant now = Instant.now(clock);
        User user = User.create(email, passwordEncoder.encode(req.password()), Role.CLIENT, now);
        userRepository.save(user);

        return issueTokens(user, now);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        var email = req.email().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isEnabled() || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        Instant now = Instant.now(clock);
        return issueTokens(user, now);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        Instant now = Instant.now(clock);
        RefreshToken rt = refreshTokenRepository.findByToken(req.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!rt.isActive(now)) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        rt.revoke();
        User user = rt.getUser();

        var access = jwtService.createAccessToken(user.getId(), user.getEmail(), user.getRole(), now);

        var newRefresh = createRefreshTokenValue();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(newRefresh)
                .expiresAt(now.plus(jwtProps.refreshTokenTtl()))
                .revoked(false)
                .createdAt(now)
                .build());

        return new AuthResponse(access, newRefresh);
    }

    @Transactional(readOnly = true)
    public InfoResponse info(Authentication auth) {
        if (auth == null) throw new IllegalArgumentException("Not authenticated");
        var role = auth.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority).orElse("UNKNOWN");
        return new InfoResponse(auth.getName(), role);
    }

    private AuthResponse issueTokens(User user, Instant now) {
        var access = jwtService.createAccessToken(user.getId(), user.getEmail(), user.getRole(), now);
        var refresh = createRefreshTokenValue();

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refresh)
                .expiresAt(now.plus(jwtProps.refreshTokenTtl()))
                .revoked(false)
                .createdAt(now)
                .build());

        return new AuthResponse(access, refresh);
    }

    private String createRefreshTokenValue() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}