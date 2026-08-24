package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationProvider;

@Configuration
@Profile("ldap")
@RequiredArgsConstructor
public class LdapSecurityConfig {

    @Value("${app.ldap.url}")
    private String url;

    @Value("${app.ldap.base}")
    private String base;

    @Value("${app.ldap.user-dn-pattern}")
    private String userDnPattern;

    @Value("${app.ldap.group-search-base}")
    private String groupSearchBase;

    @Value("${app.ldap.manager-dn:}")
    private String managerDn;

    @Value("${app.ldap.manager-password:}")
    private String managerPassword;

    @Value("${app.ldap.hr-group:hr}")
    private String hrGroup;

    @Value("${app.ldap.interviewer-group:interviewers}")
    private String interviewerGroup;

    @Value("${app.ldap.admin-group:admins}")
    private String adminGroup;

    @Bean
    LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setBase(base);
        if (!managerDn.isBlank()) {
            contextSource.setUserDn(managerDn);
            contextSource.setPassword(managerPassword);
        }
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    UserDetailsContextMapper userDetailsContextMapper() {
        return new UserDetailsContextMapper() {
            @Override
            public UserDetails mapUserFromContext(
                    DirContextOperations ctx,
                    String username,
                    java.util.Collection<? extends GrantedAuthority> authorities) {
                return new LdapUserDetailsContextAdapter(ctx, username, authorities);
            }

            @Override
            public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
            }
        };
    }

    @Bean
    AuthenticationProvider ldapAuthenticationProvider(
            BaseLdapPathContextSource contextSource,
            UserDetailsContextMapper userDetailsContextMapper) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(new FilterBasedLdapUserSearch(
                "", "(mail={0})", contextSource));

        DefaultLdapAuthoritiesPopulator authorities =
                new DefaultLdapAuthoritiesPopulator(contextSource, groupSearchBase);
        authorities.setGroupSearchFilter("(member={0})");
        authorities.setGroupRoleAttribute("cn");
        authorities.setRolePrefix("");
        authorities.setAuthorityMapper(this::mapGroupAttributes);

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator, authorities);
        provider.setUserDetailsContextMapper(userDetailsContextMapper);
        return provider;
    }

    private GrantedAuthority mapGroupAttributes(Map<String, java.util.List<String>> attributes) {
        Set<String> allValues = attributes.values().stream()
                .flatMap(java.util.Collection::stream)
                .map(v -> v.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        if (allValues.contains(adminGroup.toLowerCase(Locale.ROOT))) {
            return new SimpleGrantedAuthority("ROLE_ADMIN");
        }
        if (allValues.contains(hrGroup.toLowerCase(Locale.ROOT))) {
            return new SimpleGrantedAuthority("ROLE_HR");
        }
        if (allValues.contains(interviewerGroup.toLowerCase(Locale.ROOT))) {
            return new SimpleGrantedAuthority("ROLE_INTERVIEWER");
        }
        return new SimpleGrantedAuthority("ROLE_UNMAPPED_LDAP_GROUP");
    }
}