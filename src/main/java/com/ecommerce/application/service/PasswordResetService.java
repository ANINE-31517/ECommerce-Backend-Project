package com.ecommerce.application.service;

import com.ecommerce.application.CO.ForgotPasswordCO;
import com.ecommerce.application.CO.ResetPasswordCO;
import com.ecommerce.application.entity.PasswordResetToken;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.repository.PasswordResetTokenRepository;
import com.ecommerce.application.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${token.time}")
    private Integer tokenTime;

    @Transactional
    public void forgotPassword(ForgotPasswordCO request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            throw new BadRequestException("Email does not exist.");
        }

        if (!user.isActive()) {
            throw new BadRequestException("Account is not activated.");
        }

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(tokenTime));
        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:8080/api/reset/forgot-password?token=" + token;
        log.info("Reset Token: {}", token);

        emailService.sendEmail(user.getEmail(), "Reset Password", "Click the link to reset your password: <a href='" + resetLink + "'>Reset</a>");

    }

    @Transactional
    public void resetPassword(ResetPasswordCO request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElse(null);

        if (token == null) {
            throw new BadRequestException("Invalid token.");
        }

        User user = token.getUser();

        if(user.isLocked()) {
            throw new BadRequestException("Account is Locked!");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.deleteByUser(user);
            throw new BadRequestException("Token has expired.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info("password has been successfully reset for user with email: {} ", user.getEmail());
        tokenRepository.deleteByUser(user);
    }
}

