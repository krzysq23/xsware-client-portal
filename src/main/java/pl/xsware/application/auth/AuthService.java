package pl.xsware.application.auth;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
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
import pl.xsware.infrastructure.security.auth.AuthUtils;
import pl.xsware.infrastructure.security.jwt.JwtProperties;
import pl.xsware.infrastructure.security.jwt.JwtService;
import pl.xsware.infrastructure.web.dto.auth.*;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

@Slf4j
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
    public TokenPair register(RegisterRequest req) {
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
    public TokenPair login(LoginRequest req) {
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
    public TokenPair refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InsufficientAuthenticationException("Missing refresh token");
        }

        Instant now = Instant.now(clock);
        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
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

        return new TokenPair(access, newRefresh);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(rt -> {
                    rt.revoke();
                    refreshTokenRepository.save(rt);
                });
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, Long userId) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!currentUser.isEnabled() || !passwordEncoder.matches(request.currentPassword(), currentUser.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        currentUser.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(currentUser);

        refreshTokenRepository.revokeAllActiveByUserId(currentUser.getId());

        int revokedCount = refreshTokenRepository
                .revokeAllActiveByUserId(currentUser.getId());

        log.info("Revoked {} refresh tokens for user {}", revokedCount, currentUser.getEmail());
    }

    @Transactional(readOnly = true)
    public InfoResponse info(Authentication auth) {
        if (auth == null) throw new IllegalArgumentException("Not authenticated");
        var role = auth.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority).orElse("UNKNOWN");
        return new InfoResponse(auth.getName(), role);
    }

    private TokenPair issueTokens(User user, Instant now) {
        var access = jwtService.createAccessToken(user.getId(), user.getEmail(), user.getRole(), now);
        var refresh = createRefreshTokenValue();

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refresh)
                .expiresAt(now.plus(jwtProps.refreshTokenTtl()))
                .revoked(false)
                .createdAt(now)
                .build());

        return new TokenPair(access, refresh);
    }

    private String createRefreshTokenValue() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}