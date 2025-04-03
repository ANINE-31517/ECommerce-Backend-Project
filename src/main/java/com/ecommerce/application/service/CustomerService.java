package com.ecommerce.application.service;

import com.ecommerce.application.DTO.CustomerRegistrationRequest;
import com.ecommerce.application.entity.ActivationToken;
import com.ecommerce.application.entity.Customer;
import com.ecommerce.application.entity.Role;
import com.ecommerce.application.enums.RoleEnum;
import com.ecommerce.application.exception.CustomException;
import com.ecommerce.application.repository.ActivationTokenRepository;
import com.ecommerce.application.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Value("${token.time}")
    private Integer tokenTime;

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Transactional
    public void registerCustomer(CustomerRegistrationRequest request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("Email already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("Passwords do not match");
        }

        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setActive(false);
        customer.setContact(request.getContact());

        customer.setRoles(new Role(RoleEnum.CUSTOMER));

        customerRepository.save(customer);

        String activationTokenString = UUID.randomUUID().toString();

        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(activationTokenString);
        activationToken.setCustomer(customer);
        activationToken.setExpiryDate(LocalDateTime.now().plusHours(tokenTime));

        activationTokenRepository.save(activationToken);

        String activationLink = "http://localhost:8080/api/customers/activate?token=" + activationTokenString;
        logger.info("Activation Token: {}", activationLink);
        emailService.sendEmail(request.getEmail(), "Activate Your Account",
                "Click the link to activate: <a href='" + activationLink + "'>Activate</a>");

    }
}

