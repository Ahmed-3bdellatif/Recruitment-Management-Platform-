package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.auth.dto.AuthResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.auth.dto.LoginRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.auth.dto.RegisterRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.AuthProvider;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.Role;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleName;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserStatus;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerHashesPasswordAndAssignsDefaultRole() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("  Jane Candidate ");
        request.setEmail(" JANE@example.com ");
        request.setPassword("plain-password");
        request.setPhone("555-0100");

        Role interviewer = Role.builder().id(1L).name(RoleName.INTERVIEWER).build();
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.INTERVIEWER)).thenReturn(Optional.of(interviewer));
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(10L);
            return savedUser;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(jwtService.getExpiration()).thenReturn(java.time.Duration.ofMinutes(15));

        AuthResponse response = authService.register(request);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("jane@example.com", response.getUser().getEmail());
        assertEquals(Set.of(RoleName.INTERVIEWER), response.getUser().getRoles());
        verify(passwordEncoder).encode("plain-password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginReturnsTokenForValidActiveLocalUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("USER@example.com");
        request.setPassword("plain-password");

        User user = User.builder()
                .id(10L)
                .fullName("Jane Candidate")
                .email("user@example.com")
                .passwordHash("hashed-password")
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .roles(Set.of(Role.builder().name(RoleName.INTERVIEWER).build()))
                .build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.getExpiration()).thenReturn(java.time.Duration.ofMinutes(15));

        AuthResponse response = authService.login(request);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("user@example.com", response.getUser().getEmail());
        verify(passwordEncoder).matches("plain-password", "hashed-password");
    }

    @Test
    void loginRejectsInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong-password");

        User user = User.builder()
                .email("user@example.com")
                .passwordHash("hashed-password")
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .roles(Set.of())
                .build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void loginRejectsInactiveUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("plain-password");

        User user = User.builder()
                .email("user@example.com")
                .passwordHash("hashed-password")
                .status(UserStatus.INACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .roles(Set.of())
                .build();
        when(userRepository.findByEmail(eq("user@example.com"))).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
    }
}
