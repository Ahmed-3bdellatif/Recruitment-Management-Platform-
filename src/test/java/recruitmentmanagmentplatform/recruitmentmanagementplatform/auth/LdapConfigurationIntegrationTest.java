package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test", "ldap"})
class LdapConfigurationIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Test
    void ldapProviderIsLoadedWhenLdapProfileIsActive() {
        assertNotNull(authenticationProvider);
        assertNotNull(applicationContext.getBean(LdapAuthenticationService.class));
        assertNotNull(applicationContext.getBean(LdapSecurityConfig.class));
    }
}