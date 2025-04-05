package com.ecommerce.application.service;

import com.ecommerce.application.DTO.AdminLoginRequest;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.CustomException;
import com.ecommerce.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    public void loginAdmin(AdminLoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Invalid credentials"));

        if (user.isLocked())
            throw new CustomException("Account is locked");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);

            if (user.getInvalidAttemptCount() >= 3) {
                user.setLocked(true);
                emailService.sendEmail(user.getEmail(), "Account Locked",
                        "Your account is locked due to 3 failed login attempts.");
            }

            userRepository.save(user);
            throw new CustomException("Invalid credentials");
        }

        user.setInvalidAttemptCount(0);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        logger.info("accessToken {}", token);
    }
}
