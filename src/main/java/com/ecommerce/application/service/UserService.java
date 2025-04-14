package com.ecommerce.application.service;

import com.ecommerce.application.CO.AddressUpdateCO;
import com.ecommerce.application.CO.UpdatePasswordCO;
import com.ecommerce.application.CO.UserLoginCO;
import com.ecommerce.application.VO.AddressVO;
import com.ecommerce.application.VO.TokenResponseVO;
import com.ecommerce.application.entity.Address;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.repository.AddressRepository;
import com.ecommerce.application.repository.UserRepository;
import com.ecommerce.application.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public void updatePassword(UpdatePasswordCO request) {
        User user = SecurityUtil.getCurrentUser();

        Optional<User> userOptional = userRepository.findById(user.getId());
        if(userOptional.isEmpty()) {
            log.error("Password update failed: User not found for ID: {}", user.getId());
            throw new BadRequestException("User not found!");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.error("Password update failed: Password and Confirm Password do not match for user: {}", user.getEmail());
            throw new BadRequestException("Password and Confirm Password must match!");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        emailService.sendEmail(user.getEmail(), "Password has been Updated",
                "Your account password has been successfully updated!");

        log.info("User : {} \n Message : Your account password has been successfully updated!", user.getEmail());
    }

    public void updateAddress(AddressUpdateCO request) {
        User currentUser = SecurityUtil.getCurrentUser();

        Address address = addressRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found."));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            log.error("Address update failed: Unauthorized access by user: {}", currentUser.getEmail());
            throw new UnauthorizedException("You are not allowed to update this address.");
        }

        if (request.getCity() != null && !request.getCity().isBlank()) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null && !request.getState().isBlank()) {
            address.setState(request.getState());
        }
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            address.setCountry(request.getCountry());
        }
        if (request.getAddressLine() != null && !request.getAddressLine().isBlank()) {
            address.setAddressLine(request.getAddressLine());
        }
        if (request.getZipCode() != null && !request.getZipCode().isBlank()) {
            address.setZipCode(request.getZipCode());
        }
        if (request.getLabel() != null && !request.getLabel().isBlank()) {
            address.setLabel(request.getLabel());
        }

        addressRepository.save(address);

        log.info("User : {} \n Message : Your address has been successfully updated!", currentUser.getEmail());
    }

    public List<AddressVO> getAddresses() {
        User currentUser = SecurityUtil.getCurrentUser();

        List<Address> addresses = addressRepository.findAllByUserId(currentUser.getId());

        return addresses.stream()
                .map(this::convertToAddressVO)
                .toList();
    }

    public AddressVO convertToAddressVO(Address address) {
        return AddressVO.builder()
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .addressLine(address.getAddressLine())
                .zipCode(address.getZipCode())
                .label(address.getLabel())
                .build();
    }

    public TokenResponseVO loginUser(UserLoginCO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (user.isLocked()) {
            log.info("User Account with email: {} is locked!", user.getEmail());
            throw new UnauthorizedException("Account is locked");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);

            if (user.getInvalidAttemptCount() >= 3) {
                user.setLocked(true);
                log.info("User Account with email: {} has been locked!", user.getEmail());
                emailService.sendEmail(user.getEmail(), "Account Locked",
                        "Your account is locked due to 3 failed login attempts.");
            }

            userRepository.save(user);
            throw new BadRequestException("Invalid credentials");
        }

        user.setInvalidAttemptCount(0);
        userRepository.save(user);

        String roleName = user.getRoles().getAuthority();

        if ((roleName.equalsIgnoreCase("CUSTOMER") || roleName.equalsIgnoreCase("SELLER")) && !user.isActive()) {
            log.info("User Account with email: {} is not activated!", user.getEmail());
            throw new BadRequestException("Account is not activated");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        tokenService.saveTokenPair(user, accessToken, refreshToken);

        log.info("Login Successful! \n User: {} \n Role: {} \n AccessToken: {} \n RefreshToken: {}",
                user.getEmail(),
                roleName,
                accessToken,
                refreshToken);

        return TokenResponseVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public void logoutUser(String request) {

        if (request == null || !request.startsWith("Bearer ")) {
            log.warn("Access token is missing or invalid format!");
            throw new BadRequestException("Access token is missing or invalid format!");
        }

        String token = request.substring(7);

        if (!tokenService.isAccessTokenValid(token)) {
            log.error("Access token: {} is invalid or expired!", token);
            throw new UnauthorizedException("Invalid or expired access token!");
        }

        log.info("User Account with token: {} has been locked!", token);
        tokenService.invalidateToken(token);
    }

}
