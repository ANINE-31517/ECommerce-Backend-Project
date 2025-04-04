package com.ecommerce.application.controller;

import com.ecommerce.application.DTO.SellerLoginRequest;
import com.ecommerce.application.DTO.SellerRegistrationRequest;
import com.ecommerce.application.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody SellerRegistrationRequest request) {
        sellerService.registerSeller(request);
        return ResponseEntity.ok("Seller registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginSeller(@Valid @RequestBody SellerLoginRequest request) {
        sellerService.loginSeller(request);
        return ResponseEntity.ok("Seller logged-in successfully!");
    }

}