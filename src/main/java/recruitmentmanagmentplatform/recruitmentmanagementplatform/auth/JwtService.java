package recruitmentmanagmentplatform.recruitmentmanagementplatform.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;

@Service
public class JwtService {

    private final javax.crypto.SecretKey signingKey;

    @Getter
    private final Duration expiration;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration:900000}") long expirationMilliseconds) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("security.jwt.secret must contain at least 32 characters");
        }

        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMillis(expirationMilliseconds);
    }

    public String generateToken(User user) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expiration.toMillis());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }
}