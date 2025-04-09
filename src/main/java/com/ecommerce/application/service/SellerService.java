package com.ecommerce.application.service;

import com.ecommerce.application.CO.SellerLoginCO;
import com.ecommerce.application.CO.SellerRegistrationCO;
import com.ecommerce.application.VO.SellerRegisteredVO;
import com.ecommerce.application.VO.TokenResponseVO;
import com.ecommerce.application.constant.SellerConstant;
import com.ecommerce.application.entity.Address;
import com.ecommerce.application.entity.Role;
import com.ecommerce.application.entity.Seller;
import com.ecommerce.application.enums.RoleEnum;
import com.ecommerce.application.exception.CustomException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.repository.SellerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(SellerService.class);

    @Transactional
    public void registerSeller(SellerRegistrationCO request) {

        if (sellerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("Email already exists");
        }

        if (sellerRepository.findByGst(request.getGst()).isPresent()) {
            throw new CustomException("Gst Number already exists");
        }

        if (sellerRepository.findByCompanyName(request.getCompanyName()).isPresent()) {
            throw new CustomException("Company Name already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("Password Mismatched");
        }

        if (request.getCompanyAddress() == null ||
                isBlank(request.getCompanyAddress().getCity()) ||
                isBlank(request.getCompanyAddress().getCountry()) ||
                isBlank(request.getCompanyAddress().getAddressLine()) ||
                request.getCompanyAddress().getZipCode() == null ||
                isBlank(request.getCompanyAddress().getLabel())) {
            throw new CustomException("Address fields cannot be null or blank");
        }

        Seller seller = new Seller();
        seller.setEmail(request.getEmail());
        seller.setPassword(passwordEncoder.encode(request.getPassword()));
        seller.setGst(request.getGst());
        seller.setCompanyName(request.getCompanyName());
        seller.setCompanyContact(request.getCompanyContact());
        seller.setFirstName(request.getFirstName());
        seller.setLastName(request.getLastName());
        seller.setActive(false);
        seller.setCreatedAt(LocalDateTime.now());

        seller.setRoles(new Role(RoleEnum.SELLER));

        Address companyAddress = request.getCompanyAddress();
        companyAddress.setUser(seller);
        seller.setAddresses(List.of(companyAddress));

        sellerRepository.save(seller);
        emailService.sendEmail(request.getEmail(), "Seller Account Created",
                "Your seller account has been created and is awaiting approval.");

    }

    public TokenResponseVO loginSeller(SellerLoginCO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Seller seller = sellerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Invalid credentials"));

        if (!seller.isActive())
            throw new CustomException("Account is not activated");

        if (seller.isLocked())
            throw new CustomException("Account is locked");

        if (!passwordEncoder.matches(request.getPassword(), seller.getPassword())) {
            seller.setInvalidAttemptCount(seller.getInvalidAttemptCount() + 1);

            if (seller.getInvalidAttemptCount() >= 3) {
                seller.setLocked(true);
                emailService.sendEmail(seller.getEmail(), "Account Locked",
                        "Your account is locked due to 3 failed login attempts.");
            }

            sellerRepository.save(seller);
            throw new CustomException("Invalid credentials");
        }

        seller.setInvalidAttemptCount(0);
        sellerRepository.save(seller);

        String accessToken = jwtService.generateAccessToken(seller);
        String refreshToken = jwtService.generateRefreshToken(seller);
        logger.info("accessToken {}", accessToken);
        logger.info("refreshToken {}", refreshToken);

        tokenService.saveTokenPair(seller, accessToken, refreshToken);

        return TokenResponseVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logoutSeller(String request) {
        if (request == null || !request.startsWith("Bearer ")) {
            throw new CustomException("Access token is missing or invalid format!");
        }

        String token = request.substring(7);

        if (!tokenService.isAccessTokenValid(token)) {
            throw new UnauthorizedException("Invalid or expired access token!");
        }

        tokenService.invalidateToken(token);
    }

    public Page<SellerRegisteredVO> getAllSellers(int pageOffset, int pageSize, String sortBy, String email) {

        List<String> allowedSortFields = SellerConstant.ALLOWED_SORT_FIELDS;

        if(!allowedSortFields.contains(sortBy)) {
            throw new CustomException("Invalid Sort Type");
        }

        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sortBy));

        Page<Seller> sellers;
        if (email != null && !email.isEmpty()) {
            sellers = sellerRepository.findByEmailContainingIgnoreCase(email, pageable);
        } else {
            sellers = sellerRepository.findAll(pageable);
        }

        return sellers.map(seller -> convertToSellerRegisteredVO(seller));
    }

    private SellerRegisteredVO convertToSellerRegisteredVO(Seller seller) {
        return SellerRegisteredVO.builder()
                .id(seller.getId())
                .fullName(seller.getFullName())
                .email(seller.getEmail())
                .isActive(seller.isActive())
                .companyName(seller.getCompanyName())
                .companyContact(seller.getCompanyContact())
                .companyAddress(seller.getAddresses().getFirst())
                .build();
    }
}

