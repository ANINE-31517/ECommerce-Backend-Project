package com.ecommerce.application.controller;

import com.ecommerce.application.CO.*;
import com.ecommerce.application.VO.CustomerProfileVO;
import com.ecommerce.application.VO.ProfileUpdateVO;
import com.ecommerce.application.service.ActivationService;
import com.ecommerce.application.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody CustomerRegistrationCO request) {
        customerService.registerCustomer(request);
        return ResponseEntity.ok("Customer registered successfully. Please check your email for activation.");
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateCustomer(@RequestParam("token") String token) {
        activationService.activateCustomer(token);
        return ResponseEntity.ok("Account activated successfully");
    }

    @PostMapping("/resendActivationLink")
    public ResponseEntity<String> resendActivationLink(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        activationService.resendActivationLink(email);
        return ResponseEntity.ok("A new activation link has been sent to your email.");
    }

//    @PostMapping("/login")
//    public ResponseEntity<TokenResponseVO> loginCustomer(@Valid @RequestBody CustomerLoginCO request) {
//        TokenResponseVO responseVO = customerService.loginCustomer(request);
//        return ResponseEntity.ok(responseVO);
//    }

//    @PostMapping("/logout")
//    @PreAuthorize("hasAuthority('CUSTOMER')")
//    public ResponseEntity<String> logoutCustomer(@RequestHeader("Authorization") String request) {
//        customerService.logoutCustomer(request);
//        return ResponseEntity.ok("Logout successful. Access token is now invalidated.");
//    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CustomerProfileVO> getCustomerProfile() {
        CustomerProfileVO profile = customerService.getCustomerProfile();
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/updateProfile")
    @PreAuthorize("hasAnyAuthority('CUSTOMER')")
    public ResponseEntity<ProfileUpdateVO> updateProfile(@Valid @RequestBody CustomerProfileUpdateCO customerProfileUpdateCO) {
        ProfileUpdateVO profileUpdateVO = customerService.updateProfile(customerProfileUpdateCO);
        return ResponseEntity.ok(profileUpdateVO);
    }

    @PostMapping("/addAddress")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<String> addAddress(@Valid @RequestBody AddressCO request) {
        customerService.addAddress(request);
        return ResponseEntity.ok("Address added successfully.");
    }

    @DeleteMapping("/deleteAddress/{addressId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<String> deleteAddress(@PathVariable UUID addressId) {
        customerService.deleteAddress(addressId);
        return ResponseEntity.ok("Address deleted successfully.");
    }

}

