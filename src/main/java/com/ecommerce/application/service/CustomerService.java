package com.ecommerce.application.service;

import com.ecommerce.application.CO.CustomerLoginCO;
import com.ecommerce.application.CO.CustomerProfileUpdateCO;
import com.ecommerce.application.CO.CustomerRegistrationCO;
import com.ecommerce.application.CO.SellerProfileUpdateCO;
import com.ecommerce.application.VO.*;
import com.ecommerce.application.config.ImageStorageConfig;
import com.ecommerce.application.constant.CustomerConstant;
import com.ecommerce.application.constant.ImageConstant;
import com.ecommerce.application.entity.*;
import com.ecommerce.application.enums.RoleEnum;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.repository.ActivationTokenRepository;
import com.ecommerce.application.repository.CustomerRepository;
import com.ecommerce.application.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private final ImageStorageConfig imageStorageConfig;

    private static final List<String> allowedExtensions = ImageConstant.ALLOWED_EXTENSIONS;
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);


    @Value("${token.time}")
    private Integer tokenTime;

    @Transactional
    public void registerCustomer(CustomerRegistrationCO request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
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

        logger.info("Customer ID: {}", customer.getId());

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
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!customer.isActive())
            throw new BadRequestException("Account is not activated");

        if (customer.isLocked())
            throw new BadRequestException("Account is locked");

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            customer.setInvalidAttemptCount(customer.getInvalidAttemptCount() + 1);

            if (customer.getInvalidAttemptCount() >= 3) {
                customer.setLocked(true);
                emailService.sendEmail(customer.getEmail(), "Account Locked",
                        "Your account is locked due to 3 failed login attempts.");
            }

            customerRepository.save(customer);
            throw new BadRequestException("Invalid credentials");
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
            throw new BadRequestException("Access token is missing or invalid format!");
        }

        String token = request.substring(7);

        if (!tokenService.isAccessTokenValid(token)) {
            throw new UnauthorizedException("Invalid or expired access token!");
        }

        tokenService.invalidateToken(token);
    }

    public Page<CustomerRegisteredVO> getAllCustomers(int pageOffset, int pageSize, String sortBy, String email) {

        List<String> allowedSortFields = CustomerConstant.ALLOWED_SORT_FIELDS;

        if(!allowedSortFields.contains(sortBy)) {
            throw new BadRequestException("Invalid Sort Type");
        }

        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sortBy));

        Page<Customer> customers;
        if (email != null && !email.isEmpty()) {
            customers = customerRepository.findByEmailContainingIgnoreCase(email, pageable);
        } else {
            customers = customerRepository.findAll(pageable);
        }

        return customers.map(customer -> convertToCustomerRegisteredVO(customer));
    }

    private CustomerRegisteredVO convertToCustomerRegisteredVO(Customer customer) {
        return CustomerRegisteredVO.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .isActive(customer.isActive())
                .build();
    }

    public UserActivatedDeActivateVO activateCustomer(UUID customerId) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (customerOptional.isEmpty()) {
            throw new BadRequestException("Customer ID not found!");
        }

        Customer customer = customerOptional.get();

        if (customer.isActive()) {
            throw new BadRequestException("Customer is already activated!");
        }

        customer.setActive(true);
        customerRepository.save(customer);

        emailService.sendEmail(customer.getEmail(), "Account Activated",
                "Your account has been successfully activated!");
        return UserActivatedDeActivateVO.builder()
                .isActivated(true)
                .message("Customer account activated successfully!")
                .build();
    }

    public UserActivatedDeActivateVO deActivateCustomer(UUID customerId) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (customerOptional.isEmpty()) {
            throw new BadRequestException("Customer ID not found!");
        }

        Customer customer = customerOptional.get();

        if (!customer.isActive()) {
            throw new BadRequestException("Customer is already deActivated!");
        }

        customer.setActive(false);
        customerRepository.save(customer);

        emailService.sendEmail(customer.getEmail(), "Account deActivated",
                "Your account has been successfully deActivated!");
        return UserActivatedDeActivateVO.builder()
                .isActivated(true)
                .message("Customer account deActivated successfully!")
                .build();
    }

    public CustomerProfileVO getCustomerProfile() {
        User user = SecurityUtil.getCurrentUser();

        if (!(user instanceof Customer)) {
            throw new BadRequestException("Current user is not a customer!");
        }

        Optional<Customer> optionalCustomer = customerRepository.findById(user.getId());

        if (optionalCustomer.isEmpty()) {
            throw new BadRequestException("Customer not found!");
        }

        Customer customer = optionalCustomer.get();
        return convertToCustomerProfileVO(customer);

    }

    private CustomerProfileVO convertToCustomerProfileVO(Customer customer) {

        String imageUrl = "Image not uploaded!";

        for (String ext : allowedExtensions) {
            Path path = Paths.get(imageStorageConfig.getBasePath(), "users", customer.getId().toString() + "." + ext);
            if (Files.exists(path)) {
                imageUrl = "http://localhost:8080/api/images/users/" + customer.getId();
                break;
            }
        }

        return CustomerProfileVO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .isActive(customer.isActive())
                .image(imageUrl)
                .contact(customer.getContact())
                .build();
    }

    public ProfileUpdateVO updateProfile(CustomerProfileUpdateCO customerProfileUpdateCO) {
        User user = SecurityUtil.getCurrentUser();

        if (!(user instanceof Customer)) {
            throw new BadRequestException("Current user is not a seller!");
        }

        Optional<Customer> optionalCustomer = customerRepository.findById(user.getId());

        if (optionalCustomer.isEmpty()) {
            throw new BadRequestException("Customer not found!");
        }

        Customer customer = optionalCustomer.get();

        if (!customer.isActive()) {
            throw new BadRequestException("Account is not active. Cannot update profile.");
        }
        if (customer.isLocked()) {
            throw new BadRequestException("Account is locked. Cannot update profile.");
        }

        boolean isUpdated = false;

        if (customerProfileUpdateCO.getFirstName() != null && !customerProfileUpdateCO.getFirstName().isBlank()) {
            customer.setFirstName(customerProfileUpdateCO.getFirstName());
            isUpdated = true;
        }
        if (customerProfileUpdateCO.getLastName() != null && !customerProfileUpdateCO.getLastName().isBlank()) {
            customer.setLastName(customerProfileUpdateCO.getLastName());
            isUpdated = true;
        }
        if (customerProfileUpdateCO.getContact() != null && !customerProfileUpdateCO.getContact().isBlank()) {
            customer.setContact(customerProfileUpdateCO.getContact());
            isUpdated = true;
        }

        if (!isUpdated) {
            throw new BadRequestException("Changes are required to update the profile!");
        }

        customerRepository.save(customer);

        return ProfileUpdateVO.builder()
                .success(true)
                .message("Customer profile updated successfully!")
                .build();
    }
}

