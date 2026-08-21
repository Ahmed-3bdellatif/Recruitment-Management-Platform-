package recruitmentmanagmentplatform.recruitmentmanagementplatform.user;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        String email = normalizeEmail(user.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User email already exists");
        }

        user.setId(null);
        user.setEmail(email);

        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }

        if (user.getAuthProvider() == null) {
            user.setAuthProvider(AuthProvider.LOCAL);
        }

        if (user.getAuthProvider() == AuthProvider.LOCAL) {
            if (!StringUtils.hasText(user.getPasswordHash())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
            }
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        user.setRoles(resolveRoles(user.getRoles()));

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return findUserById(id);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(RoleName roleName) {
        return userRepository.findByRoles_Name(roleName);
    }

    public User updateUser(Long id, User updatedUser) {
        User user = findUserById(id);
        String email = normalizeEmail(updatedUser.getEmail());

        userRepository.findByEmail(email)
                .filter(existingUser -> !Objects.equals(existingUser.getId(), id))
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User email already exists");
                });

        user.setFullName(updatedUser.getFullName());
        user.setEmail(email);
        user.setPhone(updatedUser.getPhone());
        user.setAuthProvider(updatedUser.getAuthProvider());
        user.setLdapDn(updatedUser.getLdapDn());

        if (updatedUser.getStatus() != null) {
            user.setStatus(updatedUser.getStatus());
        }

        if (updatedUser.getRoles() != null) {
            user.setRoles(resolveRoles(updatedUser.getRoles()));
        }

        return userRepository.save(user);
    }

    public User updatePasswordHash(Long id, String passwordHash) {
        if (!StringUtils.hasText(passwordHash)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password hash is required");
        }

        User user = findUserById(id);
        user.setPasswordHash(passwordHash);

        return userRepository.save(user);
    }

    public User updateStatus(Long id, UserStatus status) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User status is required");
        }

        User user = findUserById(id);
        user.setStatus(status);

        return userRepository.save(user);
    }

    public User addRole(Long userId, RoleName roleName) {
        User user = findUserById(userId);
        Role role = findRoleByName(roleName);

        user.getRoles().add(role);

        return userRepository.save(user);
    }

    public User removeRole(Long userId, RoleName roleName) {
        User user = findUserById(userId);

        user.getRoles().removeIf(role -> role.getName() == roleName);

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Role findRoleByName(RoleName roleName) {
        if (roleName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role name is required");
        }

        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
    }

    private Set<Role> resolveRoles(Set<Role> roles) {
        Set<Role> resolvedRoles = new HashSet<>();

        if (roles == null) {
            return resolvedRoles;
        }

        for (Role role : roles) {
            resolvedRoles.add(findRoleByName(role.getName()));
        }

        return resolvedRoles;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User email is required");
        }

        return email.trim().toLowerCase();
    }
}
