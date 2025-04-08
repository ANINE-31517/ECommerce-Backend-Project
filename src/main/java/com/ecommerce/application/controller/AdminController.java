package com.ecommerce.application.controller;

import com.ecommerce.application.CO.AdminLoginCO;
import com.ecommerce.application.VO.TokenResponseVO;
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
    public ResponseEntity<TokenResponseVO> loginAdmin(@Valid @RequestBody AdminLoginCO request) {
        TokenResponseVO responseVO = adminService.loginAdmin(request);
        return ResponseEntity.ok(responseVO);
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> logoutSeller(@RequestHeader("Authorization") String request) {
        adminService.logoutAdmin(request);
        return ResponseEntity.ok("Logout successful. Access token is now invalidated.");
    }
}
