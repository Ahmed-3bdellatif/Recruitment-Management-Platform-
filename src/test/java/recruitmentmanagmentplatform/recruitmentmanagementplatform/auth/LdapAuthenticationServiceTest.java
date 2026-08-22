package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class LdapAuthenticationServiceTest {

    @Mock
    private AuthenticationProvider authenticationProvider;

    @Mock
    private DirContextAdapter principal;

    @Mock
    private Authentication authentication;

    @Test
        void mapsLdapAttributesAndAuthoritiesToUserProfile() throws Exception {
        when(authenticationProvider.authenticate(org.mockito.ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorities()).thenReturn((Collection) List.of(
                new SimpleGrantedAuthority("ROLE_HR")));
        when(principal.getStringAttribute("mail")).thenReturn("employee@example.com");
        when(principal.getStringAttribute("cn")).thenReturn("LDAP Employee");
        when(principal.getStringAttribute("telephoneNumber")).thenReturn("555-0102");
        when(principal.getDn()).thenReturn(new javax.naming.ldap.LdapName(
                "uid=employee,ou=people,dc=example,dc=com"));

        LdapUserProfile profile = new LdapAuthenticationService(authenticationProvider)
                .authenticate("employee@example.com", "password");

        assertEquals("employee@example.com", profile.email());
        assertEquals("LDAP Employee", profile.fullName());
        assertEquals("555-0102", profile.phone());
        assertEquals("uid=employee,ou=people,dc=example,dc=com", profile.ldapDn());
        assertEquals(java.util.Set.of(recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleName.HR),
                profile.roleNames());
    }
}