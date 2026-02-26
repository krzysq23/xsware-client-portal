package pl.xsware.infrastructure.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import pl.xsware.domain.user.Role;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "app_user")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static User create(String email, String passwordHash, Role role, Instant now) {
        return User.builder()
                .email(email.toLowerCase())
                .passwordHash(passwordHash)
                .role(role)
                .enabled(true)
                .createdAt(now)
                .build();
    }

    public void setPassword(@Nullable String encode) {
        this.passwordHash = encode;
    }
}