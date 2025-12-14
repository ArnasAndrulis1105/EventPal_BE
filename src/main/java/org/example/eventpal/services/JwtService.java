package org.example.eventpal.services;


import org.springframework.beans.factory.annotation.Value;
import org.example.eventpal.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${auth.jwt.secret.key}")
    private String SECRET_KEY;

    private static final Long TOKEN_EXPIRATION_TIME_IN_MINUTES = 144000L;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Map<String, Object> claims = Map.of("roles", roles, "userId", ((User) userDetails).getId());
        return generateToken(claims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, TOKEN_EXPIRATION_TIME_IN_MINUTES);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, Long expirationTime) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * expirationTime))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        var expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public Long getUserIdFromToken(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public boolean isUserAdmin(String token) {
        @SuppressWarnings("unchecked")
        List<String> roles = extractClaim(token, claims -> (List<String>) claims.get("roles"));
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSecretKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
