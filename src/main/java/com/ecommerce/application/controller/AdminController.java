package com.ecommerce.application.controller;

import com.ecommerce.application.CO.AdminLoginCO;
import com.ecommerce.application.exception.CustomException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.service.AdminService;
import com.ecommerce.application.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<String> loginAdmin(@Valid @RequestBody AdminLoginCO request) {
        adminService.loginAdmin(request);
        return ResponseEntity.ok("Admin logged-in successfully!");
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('ADMIN')")
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
