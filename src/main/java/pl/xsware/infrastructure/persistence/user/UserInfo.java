package pl.xsware.infrastructure.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import pl.xsware.domain.audit.AuditedEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "user_info")
public class UserInfo extends AuditedEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 50)
    private String phone;

    @Column(name = "avatar_path", length = 512)
    private String avatarPath;

    @Column(name = "avatar_content_type", length = 100)
    private String avatarContentType;

    public static UserInfo create(User user) {
        return UserInfo.builder().user(user).build();
    }

    public void updateProfile(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public void setAvatar(String path, String contentType) {
        this.avatarPath = path;
        this.avatarContentType = contentType;
    }

    public void clearAvatar() {
        this.avatarPath = null;
        this.avatarContentType = null;
    }
}