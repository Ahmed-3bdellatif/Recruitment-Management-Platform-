package recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.AuthProvider;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.Role;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleName;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private UserStatus status;

    private AuthProvider authProvider;

    private Set<RoleName> roles;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .authProvider(user.getAuthProvider())
                .roles(toRoleNames(user))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private static Set<RoleName> toRoleNames(User user) {
        if (user.getRoles() == null) {
            return Set.of();
        }

        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }
}
