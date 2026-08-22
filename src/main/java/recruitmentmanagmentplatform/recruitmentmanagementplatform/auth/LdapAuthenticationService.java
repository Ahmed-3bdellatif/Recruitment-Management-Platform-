package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleName;

@Service
@Profile("ldap")
@RequiredArgsConstructor
public class LdapAuthenticationService {

    private final AuthenticationProvider ldapAuthenticationProvider;

    public LdapUserProfile authenticate(String email, String password) {
        Authentication authentication = ldapAuthenticationProvider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        DirContextAdapter principal = (DirContextAdapter) authentication.getPrincipal();

        String resolvedEmail = attribute(principal, "mail", email);
        String fullName = attribute(principal, "cn", resolvedEmail);
        String phone = attribute(principal, "telephoneNumber", null);
        Set<RoleName> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::toRoleName)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        return new LdapUserProfile(
                resolvedEmail,
                fullName,
                phone,
                principal.getDn().toString(),
                roles);
    }

    private RoleName toRoleName(String authority) {
        try {
            return RoleName.valueOf(authority.replace("ROLE_", "").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String attribute(DirContextAdapter principal, String name, String fallback) {
        String value = principal.getStringAttribute(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}