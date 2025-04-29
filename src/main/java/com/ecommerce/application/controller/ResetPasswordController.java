package com.ecommerce.application.controller;

import com.ecommerce.application.CO.ForgotPasswordCO;
import com.ecommerce.application.CO.ResetPasswordCO;
import com.ecommerce.application.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reset")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final PasswordResetService passwordResetService;
    private final MessageSource messageSource;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordCO request) {
        passwordResetService.forgotPassword(request);
        String message = messageSource.getMessage("forget.password.link.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordCO request) {
        passwordResetService.resetPassword(request);
        String message = messageSource.getMessage("reset.password.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }
}
