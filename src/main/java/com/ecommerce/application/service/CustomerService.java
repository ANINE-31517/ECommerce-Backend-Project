package com.ecommerce.application.service;

import com.ecommerce.application.CO.CustomerLoginCO;
import com.ecommerce.application.CO.CustomerRegistrationCO;
import com.ecommerce.application.VO.TokenResponseVO;
import com.ecommerce.application.entity.ActivationToken;
import com.ecommerce.application.entity.Customer;
import com.ecommerce.application.entity.Role;
import com.ecommerce.application.enums.RoleEnum;
import com.ecommerce.application.exception.CustomException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.repository.ActivationTokenRepository;
import com.ecommerce.application.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ActivationTokenRepository activationTokenRepository;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @Value("${token.time}")
    private Integer tokenTime;

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Transactional
    public void registerCustomer(CustomerRegistrationCO request) {
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
        customer.setCreatedAt(LocalDateTime.now());

        customer.setRoles(new Role(RoleEnum.CUSTOMER));

        customerRepository.save(customer);

        String activationTokenString = UUID.randomUUID().toString();

        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(activationTokenString);
        activationToken.setCustomer(customer);
        activationToken.setExpiryDate(LocalDateTime.now().plusHours(tokenTime));

        activationTokenRepository.save(activationToken);

        String activationLink = "http://localhost:8080/api/customers/activate?token=" + activationTokenString;
        logger.info("Activation Link: {}", activationLink);
        emailService.sendEmail(request.getEmail(), "Activate Your Account",
                "Click the link to activate: <a href='" + activationLink + "'>Activate</a>");

    }

    public TokenResponseVO loginCustomer(CustomerLoginCO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Invalid credentials"));

        if (!customer.isActive())
            throw new CustomException("Account is not activated");

        if (customer.isLocked())
            throw new CustomException("Account is locked");

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            customer.setInvalidAttemptCount(customer.getInvalidAttemptCount() + 1);

            if (customer.getInvalidAttemptCount() >= 3) {
                customer.setLocked(true);
                emailService.sendEmail(customer.getEmail(), "Account Locked",
                        "Your account is locked due to 3 failed login attempts.");
            }

            customerRepository.save(customer);
            throw new CustomException("Invalid credentials");
        }

        customer.setInvalidAttemptCount(0);
        customerRepository.save(customer);

        String accessToken = jwtService.generateAccessToken(customer);
        String refreshToken = jwtService.generateRefreshToken(customer);
        logger.info("accessToken {}", accessToken);
        logger.info("refreshToken {}", refreshToken);

        tokenService.saveTokenPair(customer, accessToken, refreshToken);

        return TokenResponseVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logoutCustomer(String request) {

        if (request == null || !request.startsWith("Bearer ")) {
            throw new CustomException("Access token is missing or invalid format!");
        }

        String token = request.substring(7);

        if (!tokenService.isAccessTokenValid(token)) {
            throw new UnauthorizedException("Invalid or expired access token!");
        }

        tokenService.invalidateToken(token);
    }
}

