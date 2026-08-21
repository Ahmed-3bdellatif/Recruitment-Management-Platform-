package recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto;

import java.util.Set;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CreateUserRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    private String phone;

    private UserStatus status;

    private AuthProvider authProvider;

    private String ldapDn;

    private Set<RoleName> roleNames;

    public User toEntity() {
        return User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(password)
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
