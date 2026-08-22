package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import java.util.Set;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleName;

public record LdapUserProfile(
        String email,
        String fullName,
        String phone,
        String ldapDn,
        Set<RoleName> roleNames) {
}