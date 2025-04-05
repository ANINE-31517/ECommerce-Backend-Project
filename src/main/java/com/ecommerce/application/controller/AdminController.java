package com.ecommerce.application.controller;

import com.ecommerce.application.DTO.AdminLoginRequest;
import com.ecommerce.application.DTO.SellerLoginRequest;
import com.ecommerce.application.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<String> loginAdmin(@Valid @RequestBody AdminLoginRequest request) {
        adminService.loginAdmin(request);
        return ResponseEntity.ok("Admin logged-in successfully!");
    }
}
