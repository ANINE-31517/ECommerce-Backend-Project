package com.ecommerce.application.service;

import com.ecommerce.application.entity.ActivationToken;
import com.ecommerce.application.entity.Customer;
import com.ecommerce.application.repository.ActivationTokenRepository;
import com.ecommerce.application.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ActivationService {

    @Autowired
    private ActivationTokenRepository tokenRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public ResponseEntity<?> activateCustomer(String token) {
        Optional<ActivationToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid activation token"));
        }

        ActivationToken activationToken = tokenOpt.get();
        Customer customer = activationToken.getCustomer();

        if (activationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            String newToken = UUID.randomUUID().toString();
            ActivationToken newActivationToken = new ActivationToken();
            newActivationToken.setToken(newToken);
            newActivationToken.setCustomer(customer);
            newActivationToken.setExpiryDate(LocalDateTime.now().plusHours(24));

            tokenRepository.save(newActivationToken);
            tokenRepository.delete(activationToken);

            emailService.sendEmail(customer.getEmail(), "New Activation Link",
                    "Your activation link has expired. Use this new link: http://localhost:8080/api/customers/activate?token=" + newToken);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Activation token expired. A new activation email has been sent."));
        }

        customer.setActive(true);
        customerRepository.save(customer);

        tokenRepository.delete(activationToken);

        emailService.sendEmail(customer.getEmail(), "Account Activated",
                "Your account has been successfully activated!");

        return ResponseEntity.ok(Map.of("message", "Account activated successfully"));
    }
}

