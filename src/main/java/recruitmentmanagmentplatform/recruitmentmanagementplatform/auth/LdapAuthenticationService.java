package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Service;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleName;

@Service
@Profile("ldap")
@RequiredArgsConstructor
public class LdapAuthenticationService {

    private final AuthenticationProvider ldapAuthenticationProvider;
    private final LdapTemplate ldapTemplate;

    public LdapUserProfile authenticate(String email, String password) {
        Authentication authentication = ldapAuthenticationProvider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        DirContextOperations context = resolveUserContext(authentication.getPrincipal());

        String resolvedEmail = attribute(context, "mail", email);
        String fullName = attribute(context, "cn", resolvedEmail);
        String phone = attribute(context, "telephoneNumber", null);
        Set<RoleName> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::toRoleName)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        return new LdapUserProfile(
                resolvedEmail,
                fullName,
                phone,
                context.getDn().toString(),
                roles);
    }

    private DirContextOperations resolveUserContext(Object principal) {
        if (principal instanceof DirContextOperations context) {
            return context;
        }
        if (principal instanceof LdapUserDetails ldapUser) {
            return ldapTemplate.lookupContext(ldapUser.getDn());
        }
        throw new IllegalStateException("Unsupported LDAP principal type: " + principal.getClass().getName());
    }

    private RoleName toRoleName(String authority) {
        try {
            return RoleName.valueOf(authority.replace("ROLE_", "").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String attribute(DirContextOperations context, String name, String fallback) {
        String value = context.getStringAttribute(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
