package com.dduany.intermediary.auth;

import com.dduany.intermediary.auth.dto.LoginResponse;
import com.dduany.intermediary.config.AppProperties;
import com.dduany.intermediary.config.SecurityConfig;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final AppProperties.Auth auth;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwtEncoder;
    private final String passwordHash;

    public AuthService(AppProperties props, SecurityConfig.LoginPassword password, PasswordEncoder encoder, JwtEncoder jwtEncoder) {
        this.auth = props.auth();
        this.encoder = encoder;
        this.jwtEncoder = jwtEncoder;
        // Hash once at startup so the plain value is never compared directly.
        this.passwordHash = encoder.encode(password.value());
    }

    public LoginResponse login(String username, String password) {
        boolean userOk = auth.username().equals(username);
        // Always run the hash comparison so a wrong username costs the same time as a wrong password.
        boolean passOk = encoder.matches(password == null ? "" : password, passwordHash);
        if (!userOk || !passOk) {
            throw new BadCredentialsException("Invalid username or password");
        }
        Instant now = Instant.now();
        Instant expires = now.plus(auth.tokenTtlHours(), ChronoUnit.HOURS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("intermediary")
                .subject(username)
                .issuedAt(now)
                .expiresAt(expires)
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, username, expires);
    }
}
