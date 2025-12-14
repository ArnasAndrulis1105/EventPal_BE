package org.example.eventpal.helpers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public class JwtHelper {
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";

    public static String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            return null;
        }
        return authHeader.substring(7);
    }
}
