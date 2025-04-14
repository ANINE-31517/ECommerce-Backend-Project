package com.ecommerce.application.service;

import com.ecommerce.application.CO.RefreshTokenCO;
import com.ecommerce.application.VO.NewAccessTokenVO;
import com.ecommerce.application.entity.Token;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.repository.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    public void saveTokenPair(User user, String accessToken, String refreshToken) {
        Token token = Token.builder()
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenInvalidated(false)
                .refreshTokenInvalidated(false)
                .build();
        tokenRepository.save(token);
        log.info("Token pair saved successfully for user: {}", user.getEmail());
    }


    public boolean isAccessTokenValid(String accessToken) {
        boolean existsAndNotInvalidated = tokenRepository.findByAccessToken(accessToken)
                .map(token -> !token.isAccessTokenInvalidated())
                .orElse(false);

        return existsAndNotInvalidated && jwtService.validateAccessToken(accessToken);
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        boolean existsAndNotInvalidated = tokenRepository.findByRefreshToken(refreshToken)
                .map(token -> !token.isRefreshTokenInvalidated())
                .orElse(false);

        return existsAndNotInvalidated && jwtService.validateRefreshToken(refreshToken);
    }

    public void invalidateToken(String accessToken) {
        Token token = tokenRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new BadRequestException("Token not found"));

        token.setAccessTokenInvalidated(true);
        token.setRefreshTokenInvalidated(true);

        tokenRepository.save(token);
        log.info("Access Token: {} and Refresh Token: {} have successfully invalidated!", token.getAccessToken(), token.getRefreshToken());
    }

    @Transactional
    public NewAccessTokenVO newAccessTokenVO(RefreshTokenCO request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || !isRefreshTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        tokenRepository.findByRefreshToken(refreshToken)
                .ifPresent(token -> {
                    token.setRefreshTokenInvalidated(true);
                    token.setAccessTokenInvalidated(true);
                    tokenRepository.save(token);
                });

        String newAccessToken = jwtService.generateAccessToken((User) userDetails);
        String newRefreshToken = jwtService.generateRefreshToken((User) userDetails);

        Token newToken = Token.builder()
                .user((User) userDetails)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenInvalidated(false)
                .refreshTokenInvalidated(false)
                .build();

        tokenRepository.save(newToken);
        log.info("New Access Token: {} and New Refresh Token: {} have successfully generated!", newAccessToken, newRefreshToken);

        return NewAccessTokenVO.builder()
                .newAccessToken(newAccessToken)
                .newRefreshToken(newRefreshToken)
                .build();
    }

}
