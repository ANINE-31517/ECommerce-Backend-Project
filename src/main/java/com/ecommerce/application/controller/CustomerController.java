package com.ecommerce.application.controller;

import com.ecommerce.application.CO.*;
import com.ecommerce.application.VO.CustomerProfileVO;
import com.ecommerce.application.VO.ProfileUpdateVO;
import com.ecommerce.application.service.ActivationService;
import com.ecommerce.application.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final ActivationService activationService;
    private final MessageSource messageSource;

    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody CustomerRegistrationCO request) {
        customerService.registerCustomer(request);
        String message = messageSource.getMessage("customer.register.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateCustomer(@RequestParam("token") String token) {
        activationService.activateCustomer(token);
        String message = messageSource.getMessage("customer.activate.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/resend-activation-link")
    public ResponseEntity<String> resendActivationLink(@Valid @RequestBody Map<String, String> request) {
        String email = request.get("email");
        activationService.resendActivationLink(email);
        String message = messageSource.getMessage("customer.resend.activate.link.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CustomerProfileVO> getCustomerProfile() {
        CustomerProfileVO profile = customerService.getCustomerProfile();
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/update-profile")
    @PreAuthorize("hasAnyAuthority('CUSTOMER')")
    public ResponseEntity<ProfileUpdateVO> updateProfile(@Valid @RequestBody CustomerProfileUpdateCO customerProfileUpdateCO) {
        ProfileUpdateVO profileUpdateVO = customerService.updateProfile(customerProfileUpdateCO);
        return ResponseEntity.ok(profileUpdateVO);
    }

    @PostMapping("/add-address")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<String> addAddress(@Valid @RequestBody AddressCO request) {
        customerService.addAddress(request);
        String message = messageSource.getMessage("customer.add.address.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/delete-address/{addressId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<String> deleteAddress(@PathVariable UUID addressId) {
        customerService.deleteAddress(addressId);
        String message = messageSource.getMessage("customer.delete.address.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

}

