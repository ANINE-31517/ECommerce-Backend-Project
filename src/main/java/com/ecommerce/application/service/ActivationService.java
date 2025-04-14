package com.ecommerce.application.service;

import com.ecommerce.application.entity.ActivationToken;
import com.ecommerce.application.entity.Customer;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.repository.ActivationTokenRepository;
import com.ecommerce.application.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivationService {

    private final ActivationTokenRepository tokenRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;

    @Value("${token.time}")
    private Integer tokenTime;

    @Transactional
    public void activateCustomer(String token) {
        Optional<ActivationToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            log.error("Activation failed: Invalid token {}", token);
            throw new BadRequestException("Invalid activation token!");
        }

        ActivationToken activationToken = tokenOpt.orElseThrow(() -> new BadRequestException("Token not found"));
        Customer customer = activationToken.getCustomer();

        if(customer.isActive()) {
            log.warn("Customer with email {} is already active.", customer.getEmail());
            throw new BadRequestException("Account is already activated!");
        }

        if (activationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            String newToken = UUID.randomUUID().toString();
            ActivationToken newActivationToken = new ActivationToken();
            newActivationToken.setToken(newToken);
            newActivationToken.setCustomer(customer);
            newActivationToken.setExpiryDate(LocalDateTime.now().plusHours(tokenTime));

            tokenRepository.save(newActivationToken);
            tokenRepository.delete(activationToken);

            emailService.sendEmail(customer.getEmail(), "New Activation Link",
                    "Your activation link has expired. Use this new link: http://localhost:8080/api/customers/activate?token=" + newToken);

            log.info("New activation token has been sent to email: {}", customer.getEmail());

            throw new BadRequestException("Activation token expired. A new activation email has been sent.");
        }

        customer.setActive(true);
        customerRepository.save(customer);

        tokenRepository.delete(activationToken);

        log.info("Customer account activated successfully for email: {}", customer.getEmail());

        emailService.sendEmail(customer.getEmail(), "Account Activated",
                "Your account has been successfully activated!");

    }

    @Transactional
    public void resendActivationLink(String email) {
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);

        if (customerOpt.isEmpty()) {
            log.error("Resend activation failed email: {} not found!", email);
            throw new BadRequestException("Email not found");
        }

        Customer customer = customerOpt.orElseThrow(() -> new BadRequestException("Customer not found"));

        if (customer.isActive()) {
            log.warn("Resend activation failed email: {} is already active!", email);
            throw new BadRequestException("Account is already activated");
        }

        tokenRepository.deleteByCustomer(customer);

        String newToken = UUID.randomUUID().toString();
        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(newToken);
        activationToken.setCustomer(customer);
        activationToken.setExpiryDate(LocalDateTime.now().plusHours(tokenTime));

        tokenRepository.save(activationToken);

        String activationLink = "http://localhost:8080/api/customers/activate?token=" + newToken;
        emailService.sendEmail(customer.getEmail(), "Resend Activation Link",
                "Click the link to activate your account: <a href='" + activationLink + "'>Activate</a>");

        log.info("A new activation link has been sent to email: {}", customer.getEmail());
    }
}

