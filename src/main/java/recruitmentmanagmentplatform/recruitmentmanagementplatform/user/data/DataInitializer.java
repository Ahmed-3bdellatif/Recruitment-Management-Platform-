package recruitmentmanagmentplatform.recruitmentmanagementplatform.user.data;

import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.AuthProvider;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.Role;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleName;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserService;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserStatus;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedInitialAdmin();
    }

    private void seedRoles() {
        createRoleIfMissing(RoleName.ADMIN, "Full system administration access");
        createRoleIfMissing(RoleName.HR, "Recruitment and hiring management access");
        createRoleIfMissing(RoleName.INTERVIEWER, "Assigned interview and feedback access");
    }

    private void createRoleIfMissing(RoleName roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            roleRepository.save(Role.builder()
                    .name(roleName)
                    .description(description)
                    .build());
        }
    }

    private void seedInitialAdmin() {
        boolean hasEmail = StringUtils.hasText(adminEmail);
        boolean hasPassword = StringUtils.hasText(adminPassword);

        if (!hasEmail && !hasPassword) {
            log.info("Initial admin not created; set APP_ADMIN_EMAIL and APP_ADMIN_PASSWORD to enable bootstrap");
            return;
        }

        if (!hasEmail || !hasPassword) {
            throw new IllegalStateException("Both app.admin.email and app.admin.password are required");
        }

        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.info("Initial admin already exists for {}", normalizedEmail);
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role was not seeded"));
        userService.createUser(User.builder()
                .fullName("System Administrator")
                .email(normalizedEmail)
                .passwordHash(adminPassword)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .roles(Set.of(adminRole))
                .build());
        log.info("Initial admin created for {}", normalizedEmail);
    }
}
