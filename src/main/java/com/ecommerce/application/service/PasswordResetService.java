package com.ecommerce.application.service;

import com.ecommerce.application.DTO.ForgotPasswordRequest;
import com.ecommerce.application.DTO.ResetPasswordRequest;
import com.ecommerce.application.entity.PasswordResetToken;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.repository.PasswordResetTokenRepository;
import com.ecommerce.application.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("Email does not exist.");
        }

        if (!user.isActive()) {
            return ResponseEntity.badRequest().body("Account is not activated.");
        }

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(3));
        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:8080/api/reset/forgot-password?token=" + token;
        emailService.sendEmail(user.getEmail(), "Reset Password", "Click the link to reset your password: <a href='" + resetLink + "'>Reset</a>");

        return ResponseEntity.ok("Password reset email sent.");
    }

    @Transactional
    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElse(null);

        if (token == null) {
            return ResponseEntity.badRequest().body("Invalid token.");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            return ResponseEntity.badRequest().body("Token has expired.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        tokenRepository.delete(token);

        return ResponseEntity.ok("Password has been successfully reset.");
    }
}

