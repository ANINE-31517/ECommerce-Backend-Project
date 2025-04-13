package com.ecommerce.application.controller;

import com.ecommerce.application.CO.ForgotPasswordCO;
import com.ecommerce.application.CO.ResetPasswordCO;
import com.ecommerce.application.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reset")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordCO request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok("Password reset email sent.");
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordCO request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok("Password has been successfully reset.");
    }
}
