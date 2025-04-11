package com.ecommerce.application.controller;

import com.ecommerce.application.CO.ResetPasswordCO;
import com.ecommerce.application.CO.UpdatePasswordCO;
import com.ecommerce.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/updatePassword")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordCO request) {
        userService.updatePassword(request);
        return ResponseEntity.ok("Password has been successfully updated.");
    }
}
