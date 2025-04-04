package com.ecommerce.application.service;

import com.ecommerce.application.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    @Value("${secret.key}")
    private String key;

    @Value("${access.token.time}")
    private Integer accessTime;

    private final String SECRET_KEY = key;

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(accessTime, ChronoUnit.HOURS)))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}

