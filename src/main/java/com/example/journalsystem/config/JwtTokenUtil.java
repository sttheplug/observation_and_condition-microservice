package com.example.journalsystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {

    private final JwtDecoder jwtDecoder;

    @Autowired
    public JwtTokenUtil(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }
    public String extractUsernameFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getClaimAsString("preferred_username");
        } catch (JwtException ex) {
            throw new IllegalArgumentException("Invalid token", ex);
        }
    }
}
