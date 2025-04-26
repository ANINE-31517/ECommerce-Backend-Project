package com.ecommerce.application.service;

import com.ecommerce.application.CO.AddressCO;
import com.ecommerce.application.CO.SellerProfileUpdateCO;
import com.ecommerce.application.CO.SellerRegistrationCO;
import com.ecommerce.application.VO.*;
import com.ecommerce.application.config.ImageStorageConfig;
import com.ecommerce.application.constant.ImageConstant;
import com.ecommerce.application.constant.UserConstant;
import com.ecommerce.application.entity.Address;
import com.ecommerce.application.entity.Role;
import com.ecommerce.application.entity.Seller;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.enums.RoleEnum;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.repository.SellerRepository;
import com.ecommerce.application.repository.UserRepository;
import com.ecommerce.application.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {

    private final SellerRepository sellerRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageConfig imageStorageConfig;
    private final UserRepository userRepository;

    private static final List<String> allowedExtensions = ImageConstant.ALLOWED_EXTENSIONS;

    @Transactional
    public void registerSeller(SellerRegistrationCO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("Registration failed email {} already exists!", request.getEmail());
            throw new BadRequestException("Email already exists");
        }

        if (sellerRepository.findByGst(request.getGst()).isPresent()) {
            log.error("Registration failed gst {} already exists!", request.getGst());
            throw new BadRequestException("Gst Number already exists");
        }

        if (sellerRepository.findByCompanyName(request.getCompanyName()).isPresent()) {
            log.error("Registration failed company name {} already exists!", request.getCompanyName());
            throw new BadRequestException("Company Name already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.error("Registration failed: Passwords do not match for email: {}", request.getEmail());
            throw new BadRequestException("Password Mismatched");
        }

        if (request.getCompanyAddress() == null ||
                isBlank(request.getCompanyAddress().getCity()) ||
                isBlank(request.getCompanyAddress().getCountry()) ||
                isBlank(request.getCompanyAddress().getAddressLine()) ||
                request.getCompanyAddress().getZipCode() == null ||
                isBlank(request.getCompanyAddress().getLabel())) {
            log.info("Valid address is required with not null fields!");
            throw new BadRequestException("Address fields cannot be null or blank");
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

        seller.setRoles(new Role(RoleEnum.SELLER));

        AddressCO addressCO = request.getCompanyAddress();

        Address companyAddress = new Address();
        companyAddress.setCity(addressCO.getCity());
        companyAddress.setState(addressCO.getState());
        companyAddress.setCountry(addressCO.getCountry());
        companyAddress.setAddressLine(addressCO.getAddressLine());
        companyAddress.setZipCode(addressCO.getZipCode());
        companyAddress.setLabel(addressCO.getLabel());

        companyAddress.setUser(seller);
        seller.setAddresses(List.of(companyAddress));

        sellerRepository.save(seller);

        log.info("Seller ID: {}", seller.getId());
        log.info("Seller address ID: {}", seller.getAddresses().getFirst().getId());

        emailService.sendEmail(request.getEmail(), "Seller Account Created",
                "Your seller account has been created and is awaiting approval.");

        log.info("Registration Successful! \n User: {} \n Role: {}",
                seller.getEmail(),
                seller.getRoles().getAuthority());
    }

    public Page<SellerRegisteredVO> getAllSellers(int pageOffset, int pageSize, String sortBy, String email) {
        log.info("Fetching sellers - pageOffset: {}, pageSize: {}, sortBy: {}, emailFilter: {}", pageOffset, pageSize, sortBy, email);

        List<String> allowedSortFields = UserConstant.ALLOWED_SORT_FIELDS;

        if (!allowedSortFields.contains(sortBy)) {
            log.error("Invalid sort type is passed, choose among (dateCreated)");
            throw new BadRequestException("Invalid Sort Type");
        }

        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sortBy));

        Page<Seller> sellers;
        if (email != null && !email.isEmpty()) {
            sellers = sellerRepository.findByEmailContainingIgnoreCase(email, pageable);
        } else {
            sellers = sellerRepository.findAll(pageable);
        }
        log.info("Total sellers fetched: {}", sellers.getTotalElements());

        return sellers.map(this::convertToSellerRegisteredVO);
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

    public UserActivatedDeActivateVO activateSeller(UUID sellerId) {
        Optional<Seller> sellerOptional = sellerRepository.findById(sellerId);

        if (sellerOptional.isEmpty()) {
            log.error("Seller Id not found!");
            throw new ResourceNotFoundException("Seller ID not found!");
        }

        Seller seller = sellerOptional.get();

        if (seller.isActive()) {
            log.error("Seller with Id {} is already activated!", seller.getId());
            throw new BadRequestException("Seller is already activated!");
        }

        seller.setActive(true);
        sellerRepository.save(seller);

        emailService.sendEmail(seller.getEmail(), "Account Activated",
                "Your account has been successfully activated!");
        log.info("Seller with email: {} has been successfully activated!", seller.getEmail());

        return UserActivatedDeActivateVO.builder()
                .isActivated(true)
                .message("Seller account activated successfully!")
                .build();
    }

    public UserActivatedDeActivateVO deActivateSeller(UUID sellerId) {
        Optional<Seller> sellerOptional = sellerRepository.findById(sellerId);

        if (sellerOptional.isEmpty()) {
            log.error("Seller id not found!");
            throw new ResourceNotFoundException("Seller ID not found!");
        }

        Seller seller = sellerOptional.get();

        if (!seller.isActive()) {
            log.error("Seller with Id {} is already deActivated!", seller.getId());
            throw new BadRequestException("Seller is already deActivated!");
        }

        seller.setActive(false);
        sellerRepository.save(seller);

        emailService.sendEmail(seller.getEmail(), "Account deActivated",
                "Your account has been successfully deActivated!");
        log.info("Seller with email: {} has been successfully deActivated!", seller.getEmail());

        return UserActivatedDeActivateVO.builder()
                .isActivated(true)
                .message("Seller account deActivated successfully!")
                .build();
    }

    public SellerProfileVO getSellerProfile() {
        User user = SecurityUtil.getCurrentUser();

        if (!(user instanceof Seller)) {
            log.error("User with email {} is not a seller!", user.getEmail());
            throw new UnauthorizedException("Current user is not a seller!");
        }

        Optional<Seller> optionalSeller = sellerRepository.findById(user.getId());

        if (optionalSeller.isEmpty()) {
            log.error("Seller not found!");
            throw new ResourceNotFoundException("Seller not found!");
        }

        Seller seller = optionalSeller.get();
        return convertToSellerProfileVO(seller);
    }

    private SellerProfileVO convertToSellerProfileVO(Seller seller) {
        String imageUrl = "Image not uploaded!";

        for (String ext : allowedExtensions) {
            Path path = Paths.get(imageStorageConfig.getBasePath(), "users", seller.getId().toString() + "." + ext);
            if (Files.exists(path)) {
                imageUrl = "http://localhost:8080/api/images/users/" + seller.getId();
                break;
            }
        }

        return SellerProfileVO.builder()
                .id(seller.getId())
                .firstName(seller.getFirstName())
                .lastName(seller.getLastName())
                .isActive(seller.isActive())
                .companyName(seller.getCompanyName())
                .companyContact(seller.getCompanyContact())
                .gst(seller.getGst())
                .image(imageUrl)
                .address(seller.getAddresses().getFirst())
                .build();
    }

    public ProfileUpdateVO updateProfile(SellerProfileUpdateCO sellerProfileUpdateCO) {
        User user = SecurityUtil.getCurrentUser();

        if (!(user instanceof Seller)) {
            log.error("User with the email {} is not a seller!", user.getEmail());
            throw new UnauthorizedException("Current user is not a seller!");
        }

        Optional<Seller> optionalSeller = sellerRepository.findById(user.getId());

        if (optionalSeller.isEmpty()) {
            log.error("Seller does not found!");
            throw new ResourceNotFoundException("Seller not found!");
        }

        Seller seller = optionalSeller.get();

        if (!seller.isActive()) {
            log.warn("Profile update failed: Account is not active. User ID: {}", seller.getId());
            throw new BadRequestException("Account is not active. Cannot update profile.");
        }
        if (seller.isLocked()) {
            log.warn("Profile update failed: Account is locked. User ID: {}", seller.getId());
            throw new BadRequestException("Account is locked. Cannot update profile.");
        }

        boolean isUpdated = false;

        if (sellerProfileUpdateCO.getFirstName() != null && !sellerProfileUpdateCO.getFirstName().isBlank()) {
            seller.setFirstName(sellerProfileUpdateCO.getFirstName());
            isUpdated = true;
        }
        if (sellerProfileUpdateCO.getLastName() != null && !sellerProfileUpdateCO.getLastName().isBlank()) {
            seller.setLastName(sellerProfileUpdateCO.getLastName());
            isUpdated = true;
        }
        if (sellerProfileUpdateCO.getCompanyName() != null && !sellerProfileUpdateCO.getCompanyName().isBlank()) {
            boolean companyExists = sellerRepository.existsByCompanyName(sellerProfileUpdateCO.getCompanyName());

            if (companyExists) {
                throw new BadRequestException("Company name already exists! Please choose another one.");
            }
            seller.setCompanyName(sellerProfileUpdateCO.getCompanyName());
            isUpdated = true;
        }
        if (sellerProfileUpdateCO.getCompanyContact() != null && !sellerProfileUpdateCO.getCompanyContact().isBlank()) {
            seller.setCompanyContact(sellerProfileUpdateCO.getCompanyContact());
            isUpdated = true;
        }

        if (!isUpdated) {
            log.error("At least one field is required to be changed for updating the profile!");
            throw new BadRequestException("Changes are required to update the profile!");
        }

        sellerRepository.save(seller);
        log.info("Seller profile with email: {} has been successfully updated!", seller.getEmail());

        return ProfileUpdateVO.builder()
                .success(true)
                .message("Seller profile updated successfully!")
                .build();
    }

}

