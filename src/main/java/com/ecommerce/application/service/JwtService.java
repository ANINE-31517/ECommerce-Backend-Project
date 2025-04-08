package com.ecommerce.application.service;

import com.ecommerce.application.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    @Value("${access.token.time}")
    private Integer accessTime;

    @Value("${refresh.token.time}")
    private Integer refreshTime;

    public JwtService(@Value("${secret.key}") String key) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(key));
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessTime, "ACCESS");
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTime, "REFRESH");
    }

    public String generateToken(User user, Integer time, String tokenType) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRoles().getAuthority())
                .claim("tokenType", tokenType)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(time, ChronoUnit.HOURS)))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String tokenType = claims.get("tokenType", String.class);
            return "ACCESS".equals(tokenType);
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}

