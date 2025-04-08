package com.ecommerce.application.controller;

import com.ecommerce.application.CO.CustomerLoginCO;
import com.ecommerce.application.CO.CustomerRegistrationCO;
import com.ecommerce.application.VO.TokenResponseVO;
import com.ecommerce.application.exception.CustomException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.service.ActivationService;
import com.ecommerce.application.service.CustomerService;
import com.ecommerce.application.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final ActivationService activationService;
    private final TokenService tokenService;

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

    @PostMapping("/resend-activation-link")
    public ResponseEntity<String> resendActivationLink(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        activationService.resendActivationLink(email);
        return ResponseEntity.ok("A new activation link has been sent to your email.");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseVO> loginCustomer(@Valid @RequestBody CustomerLoginCO request) {
        TokenResponseVO responseVO = customerService.loginCustomer(request);
        return ResponseEntity.ok(responseVO);
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<String> logoutCustomer(@RequestHeader("Authorization") String request) {
        customerService.logoutCustomer(request);
        return ResponseEntity.ok("Logout successful. Access token is now invalidated.");
    }

}

