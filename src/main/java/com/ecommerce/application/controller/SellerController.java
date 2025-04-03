package com.ecommerce.application.controller;

import com.ecommerce.application.DTO.SellerRegistrationRequest;
import com.ecommerce.application.service.SellerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody SellerRegistrationRequest request) {
        sellerService.registerSeller(request);
        return ResponseEntity.ok("Seller registered successfully.");
    }

}