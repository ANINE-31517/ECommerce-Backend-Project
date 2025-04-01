package com.ecommerce.application.controller;

import com.ecommerce.application.DTO.CustomerRegistrationRequest;
import com.ecommerce.application.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody CustomerRegistrationRequest request) {
        customerService.registerCustomer(request);
        return ResponseEntity.ok("Customer registered successfully. Please check your email for activation.");
    }
}

