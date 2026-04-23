package com.juanbenevento.wms.shared.infrastructure.security;

import com.juanbenevento.wms.identity.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final int MIN_SECRET_KEY_LENGTH = 32; // 256 bits minimum for HS256

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @PostConstruct
    public void validateSecretKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET_KEY is not configured. " +
                "Set the environment variable JWT_SECRET_KEY with a Base64-encoded key of at least 32 characters."
            );
        }
        
        // Validate minimum length for HS256
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            if (keyBytes.length < MIN_SECRET_KEY_LENGTH) {
                throw new IllegalStateException(
                    "JWT_SECRET_KEY is too short. Minimum length is 256 bits (32 bytes Base64-encoded, ~44 characters)."
                );
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "JWT_SECRET_KEY is not valid Base64. Please provide a Base64-encoded secret key."
            );
        }
    }

    // --- EXTRAER DATOS ---
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenantId", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // --- GENERAR TOKEN (Sobrecarga para Dominio) ---
    // Este es el que usa tu AuthenticationService
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("tenantId", user.getTenantId());

        return buildToken(extraClaims, user.getUsername());
    }

    // --- GENERAR TOKEN (Sobrecarga para UserDetails estándar) ---
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails.getUsername());
    }

    // --- MÉTODO PRIVADO DE CONSTRUCCIÓN (Para no repetir código) ---
    private String buildToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    // --- VALIDACIÓN ---
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}