package com.ecommerce.application.service;

import com.ecommerce.application.CO.UpdatePasswordCO;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.repository.UserRepository;
import com.ecommerce.application.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updatePassword(UpdatePasswordCO request) {
        User user = SecurityUtil.getCurrentUser();

        Optional<User> userOptional = userRepository.findById(user.getId());
        if(userOptional.isEmpty()) {
            throw new BadRequestException("User not found!");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and Confirm Password must match!");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        emailService.sendEmail(user.getEmail(), "Password has been Updated",
                "Your account password has been successfully updated!");
    }
}
