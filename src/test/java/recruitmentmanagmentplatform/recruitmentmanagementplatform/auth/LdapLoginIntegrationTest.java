package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "ldap"})
class LdapLoginIntegrationTest {

    private static final InMemoryDirectoryServer LDAP_SERVER;

    static {
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=com");
            config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("ldap", 0));
            config.setEnforceSingleStructuralObjectClass(false);
            config.setEnforceAttributeSyntaxCompliance(false);

            LDAP_SERVER = new InMemoryDirectoryServer(config);
            LDAP_SERVER.importFromLDIF(
                    true,
                    java.nio.file.Path.of("ldap/bootstrap/example.ldif").toAbsolutePath().toString());
            LDAP_SERVER.startListening();
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void ldapProperties(DynamicPropertyRegistry registry) {
        registry.add("app.ldap.url", () -> "ldap://localhost:" + LDAP_SERVER.getListenPort());
    }

    @AfterAll
    static void stopLdap() {
        LDAP_SERVER.shutDown(true);
    }

    @Test
    void ldapLoginReturnsJwt() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "employee@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("employee@example.com"))
                .andExpect(jsonPath("$.user.roles[0]").value("HR"));
    }

    @Test
    void ldapLoginRejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "employee@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
