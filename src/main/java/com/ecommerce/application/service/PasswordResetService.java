package com.ecommerce.application.service;

import com.ecommerce.application.DTO.ForgotPasswordRequest;
import com.ecommerce.application.DTO.ResetPasswordRequest;
import com.ecommerce.application.entity.PasswordResetToken;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.CustomException;
import com.ecommerce.application.repository.PasswordResetTokenRepository;
import com.ecommerce.application.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${token.time}")
    private Integer tokenTime;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            throw new CustomException("Email does not exist.");
        }

        if (!user.isActive()) {
            throw new CustomException("Account is not activated.");
        }

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(tokenTime));
        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:8080/api/reset/forgot-password?token=" + token;
        emailService.sendEmail(user.getEmail(), "Reset Password", "Click the link to reset your password: <a href='" + resetLink + "'>Reset</a>");

    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElse(null);

        if (token == null) {
            throw new CustomException("Invalid token.");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new CustomException("Token has expired.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("Passwords do not match.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        tokenRepository.delete(token);

    }
}

