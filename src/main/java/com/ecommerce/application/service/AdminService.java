package com.ecommerce.application.service;

import com.ecommerce.application.CO.AdminLoginCO;
import com.ecommerce.application.VO.TokenResponseVO;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    public TokenResponseVO loginAdmin(AdminLoginCO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (user.isLocked())
            throw new BadRequestException("Account is locked");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);

            if (user.getInvalidAttemptCount() >= 3) {
                user.setLocked(true);
                emailService.sendEmail(user.getEmail(), "Account Locked",
                        "Your account is locked due to 3 failed login attempts.");
            }

            userRepository.save(user);
            throw new BadRequestException("Invalid credentials");
        }

        user.setInvalidAttemptCount(0);
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        logger.info("accessToken {}", accessToken);
        logger.info("refreshToken {}", refreshToken);

        tokenService.saveTokenPair(user, accessToken, refreshToken);

        return TokenResponseVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logoutAdmin(String request) {
        if (request == null || !request.startsWith("Bearer ")) {
            throw new BadRequestException("Access token is missing or invalid format!");
        }

        String token = request.substring(7);

        if (!tokenService.isAccessTokenValid(token)) {
            throw new UnauthorizedException("Invalid or expired access token!");
        }

        tokenService.invalidateToken(token);
    }
}
