package com.ecommerce.application.service;

import com.ecommerce.application.CO.RefreshTokenCO;
import com.ecommerce.application.VO.NewAccessTokenVO;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final Set<String> invalidatedTokens = new HashSet<>();
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public boolean isTokenValid(String token) {
        if (invalidatedTokens.contains(token)) {
            return false;
        }
        return jwtService.validateToken(token);
    }

    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
    }

    public NewAccessTokenVO newAccessTokenVO(RefreshTokenCO request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || !isTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String newAccessToken = jwtService.generateAccessToken((User) userDetails);

        return NewAccessTokenVO.builder()
                .newAccessToken(newAccessToken)
                .build();
    }
}
