package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto.UserResponse;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    private String tokenType;

    private long expiresIn;

    private UserResponse user;
}