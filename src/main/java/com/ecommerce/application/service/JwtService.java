package com.ecommerce.application.service;

import com.ecommerce.application.entity.User;
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

    @Value("${secret.key}")
    private String key;

    @Value("${access.token.time}")
    private Integer accessTime;

    public String generateToken(User user) {

        SecretKey secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(key));

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(accessTime, ChronoUnit.HOURS)))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}

