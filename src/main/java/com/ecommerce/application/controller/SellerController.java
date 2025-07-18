package com.ecommerce.application.controller;

import com.ecommerce.application.CO.SellerProfileUpdateCO;
import com.ecommerce.application.CO.SellerRegistrationCO;
import com.ecommerce.application.VO.ProfileUpdateVO;
import com.ecommerce.application.VO.SellerProfileVO;
import com.ecommerce.application.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final MessageSource messageSource;

    @PostMapping("/register")
    public ResponseEntity<String> registerSeller(@Valid @RequestBody SellerRegistrationCO request) {
        sellerService.registerSeller(request);
        String message = messageSource.getMessage("seller.register.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<SellerProfileVO> getSellerProfile() {
        SellerProfileVO profile = sellerService.getSellerProfile();
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/update-profile")
    @PreAuthorize("hasAnyAuthority('SELLER')")
    public ResponseEntity<ProfileUpdateVO> updateProfile(@Valid @RequestBody SellerProfileUpdateCO sellerProfileUpdateCO) {
        ProfileUpdateVO profileUpdateVO = sellerService.updateProfile(sellerProfileUpdateCO);
        return ResponseEntity.ok(profileUpdateVO);
    }

}