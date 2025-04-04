package com.ecommerce.application.controller;

import com.ecommerce.application.DTO.CustomerLoginRequest;
import com.ecommerce.application.DTO.CustomerRegistrationRequest;
import com.ecommerce.application.service.ActivationService;
import com.ecommerce.application.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final ActivationService activationService;

    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody CustomerRegistrationRequest request) {
        customerService.registerCustomer(request);
        return ResponseEntity.ok("Customer registered successfully. Please check your email for activation.");
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateCustomer(@RequestParam("token") String token) {
        activationService.activateCustomer(token);
        return ResponseEntity.ok("Account activated successfully");
    }

    @PostMapping("/resend-activation-link")
    public ResponseEntity<String> resendActivationLink(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        activationService.resendActivationLink(email);
        return ResponseEntity.ok("A new activation link has been sent to your email.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody CustomerLoginRequest request) {
        customerService.loginCustomer(request);
        return ResponseEntity.ok("Customer logged-in successfully!");
    }

}

