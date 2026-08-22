package recruitmentmanagmentplatform.recruitmentmanagementplatform.dev;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import java.nio.file.Path;

public final class EmbeddedLdapServer {

    private EmbeddedLdapServer() {
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8389;
        Path ldif = Path.of("ldap/bootstrap/example.ldif").toAbsolutePath();

        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=com");
        config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("ldap", port));
        config.setEnforceSingleStructuralObjectClass(false);
        config.setEnforceAttributeSyntaxCompliance(false);

        InMemoryDirectoryServer server = new InMemoryDirectoryServer(config);
        server.importFromLDIF(true, ldif.toString());
        server.startListening();

        System.out.println("Embedded LDAP server listening on ldap://localhost:" + port);
        System.out.println("Test user: employee@example.com / password123");
        Thread.currentThread().join();
    }
}
