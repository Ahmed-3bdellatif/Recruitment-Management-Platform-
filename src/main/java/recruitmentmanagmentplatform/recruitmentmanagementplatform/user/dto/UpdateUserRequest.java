package recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.AuthProvider;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.Role;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleName;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String fullName;

    private String email;

    private String phone;

    private UserStatus status;

    private AuthProvider authProvider;

    private String ldapDn;

    private Set<RoleName> roleNames;

    public User toEntity() {
        return User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .status(status)
                .authProvider(authProvider)
                .ldapDn(ldapDn)
                .roles(toRoles(roleNames))
                .build();
    }

    private Set<Role> toRoles(Set<RoleName> roleNames) {
        if (roleNames == null) {
            return null;
        }

        return roleNames.stream()
                .map(roleName -> Role.builder().name(roleName).build())
                .collect(java.util.stream.Collectors.toSet());
    }
}
