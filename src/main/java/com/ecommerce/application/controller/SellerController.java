package com.ecommerce.application.controller;

import com.ecommerce.application.DTO.SellerLoginRequest;
import com.ecommerce.application.DTO.SellerRegistrationRequest;
import com.ecommerce.application.exception.CustomException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.service.SellerService;
import com.ecommerce.application.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final TokenService tokenService;

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

    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<String> logoutSeller(@RequestHeader("Authorization") String request) {
        if (request == null || !request.startsWith("Bearer ")) {
            throw new CustomException("Access token is missing or invalid format!");
        }

        String token = request.substring(7);

        if (!tokenService.isTokenValid(token)) {
            throw new UnauthorizedException("Invalid or expired access token!");
        }

        tokenService.invalidateToken(token);

        return ResponseEntity.ok("Logout successful. Access token is now invalidated.");
    }

}