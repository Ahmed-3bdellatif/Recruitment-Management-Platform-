package recruitmentmanagmentplatform.recruitmentmanagementplatform.user;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto.CreateUserRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto.UpdateUserRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto.UserResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> getUsers(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) RoleName role) {
        List<User> users;
        if (status != null) {
            users = userService.getUsersByStatus(status);
        } else if (role != null) {
            users = userService.getUsersByRole(role);
        } else {
            users = userService.getAllUsers();
        }

        return users.stream().map(UserResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return UserResponse.fromEntity(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.fromEntity(userService.updateUser(id, request.toEntity()));
    }

    @PutMapping("/{id}/status")
    public UserResponse updateStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        return UserResponse.fromEntity(userService.updateStatus(id, status));
    }

    @PostMapping("/{id}/roles")
    public UserResponse addRole(
            @PathVariable Long id,
            @RequestParam RoleName role) {
        return UserResponse.fromEntity(userService.addRole(id, role));
    }

    @DeleteMapping("/{id}/roles")
    public UserResponse removeRole(
            @PathVariable Long id,
            @RequestParam RoleName role) {
        return UserResponse.fromEntity(userService.removeRole(id, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
