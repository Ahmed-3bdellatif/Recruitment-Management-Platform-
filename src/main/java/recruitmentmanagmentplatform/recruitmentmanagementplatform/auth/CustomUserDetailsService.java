package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = normalizeEmail(username);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                        .toList())
                .accountLocked(user.getStatus() == recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserStatus.LOCKED)
                .disabled(user.getStatus() == recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserStatus.INACTIVE)
                .build();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("User not found");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
