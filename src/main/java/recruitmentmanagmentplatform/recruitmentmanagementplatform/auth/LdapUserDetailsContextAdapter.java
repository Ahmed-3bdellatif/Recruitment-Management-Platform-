package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import java.util.Collection;
import java.util.Collections;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

public class LdapUserDetailsContextAdapter implements LdapUserDetails {

    private final DirContextOperations context;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public LdapUserDetailsContextAdapter(
            DirContextOperations context,
            String username,
            Collection<? extends GrantedAuthority> authorities) {
        this.context = context;
        this.username = username;
        this.authorities = authorities == null ? Collections.emptyList() : authorities;
    }

    public DirContextOperations getContext() {
        return context;
    }

    @Override
    public String getDn() {
        return context.getDn() != null ? context.getDn().toString() : "";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void eraseCredentials() {
    }
}
