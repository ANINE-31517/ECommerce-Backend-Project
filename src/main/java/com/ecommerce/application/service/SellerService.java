package com.ecommerce.application.service;

import com.ecommerce.application.DTO.SellerRegistrationRequest;
import com.ecommerce.application.entity.Address;
import com.ecommerce.application.entity.Role;
import com.ecommerce.application.entity.Seller;
import com.ecommerce.application.enums.RoleEnum;
import com.ecommerce.application.exception.CustomException;
import com.ecommerce.application.repository.SellerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseEntity<?> registerSeller(SellerRegistrationRequest request) {

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

        seller.setRoles(new Role(RoleEnum.SELLER));

        Address companyAddress = request.getCompanyAddress();
        companyAddress.setUser(seller);
        seller.setAddresses(List.of(companyAddress));

        sellerRepository.save(seller);
        emailService.sendEmail(request.getEmail(), "Seller Account Created",
                "Your seller account has been created and is awaiting approval.");

        return ResponseEntity.ok(Map.of("message", "Seller registered successfully. Awaiting approval."));
    }
}

