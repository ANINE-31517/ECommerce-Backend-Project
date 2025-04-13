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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


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

    public void updateAddress(AddressUpdateCO request) {
        User currentUser = SecurityUtil.getCurrentUser();

        Address address = addressRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found."));

        if (!address.getUser().getId().equals(currentUser.getId())) {
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
    }

    public List<AddressVO> getAddresses() {
        User currentUser = SecurityUtil.getCurrentUser();

        List<Address> addresses = addressRepository.findAllByUserId(currentUser.getId());

        return addresses.stream()
                .map(this::convertToAddressVO)
                .collect(Collectors.toList());
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

        if (user.isLocked())
            throw new BadRequestException("Account is locked");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);

            if (user.getInvalidAttemptCount() >= 3) {
                user.setLocked(true);
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
            throw new BadRequestException("Account is not activated");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        tokenService.saveTokenPair(user, accessToken, refreshToken);

        logger.info("Login Successful! \n User: {} \n Role: {} \n AccessToken: {} \n RefreshToken: {}",
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
            throw new BadRequestException("Access token is missing or invalid format!");
        }

        String token = request.substring(7);

        if (!tokenService.isAccessTokenValid(token)) {
            throw new UnauthorizedException("Invalid or expired access token!");
        }

        tokenService.invalidateToken(token);
    }

}
