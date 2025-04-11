package com.ecommerce.application.controller;

import com.ecommerce.application.CO.SellerLoginCO;
import com.ecommerce.application.CO.SellerProfileUpdateCO;
import com.ecommerce.application.CO.SellerRegistrationCO;
import com.ecommerce.application.VO.ProfileUpdateVO;
import com.ecommerce.application.VO.SellerProfileVO;
import com.ecommerce.application.VO.TokenResponseVO;
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
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody SellerRegistrationCO request) {
        sellerService.registerSeller(request);
        return ResponseEntity.ok("Seller registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseVO> loginSeller(@Valid @RequestBody SellerLoginCO request) {
        TokenResponseVO responseVO = sellerService.loginSeller(request);
        return ResponseEntity.ok(responseVO);
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<String> logoutSeller(@RequestHeader("Authorization") String request) {
        sellerService.logoutSeller(request);
        return ResponseEntity.ok("Logout successful. Access token is now invalidated.");
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<SellerProfileVO> getSellerProfile() {
        SellerProfileVO profile = sellerService.getSellerProfile();
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/updateProfile")
    @PreAuthorize("hasAnyAuthority('SELLER')")
    public ResponseEntity<ProfileUpdateVO> updateProfile(@Valid @RequestBody SellerProfileUpdateCO sellerProfileUpdateCO) {
        ProfileUpdateVO profileUpdateVO = sellerService.updateProfile(sellerProfileUpdateCO);
        return ResponseEntity.ok(profileUpdateVO);
    }

}