package com.ecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final Set<String> invalidatedTokens = new HashSet<>();
    private final JwtService jwtService;

    public boolean isTokenValid(String token) {
        if (invalidatedTokens.contains(token)) {
            return false;
        }
        return jwtService.validateToken(token);
    }

    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
    }
}
