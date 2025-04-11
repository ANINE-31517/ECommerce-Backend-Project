package com.ecommerce.application.controller;

import com.ecommerce.application.CO.AddressUpdateCO;
import com.ecommerce.application.CO.UpdatePasswordCO;
import com.ecommerce.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/updatePassword")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody UpdatePasswordCO request) {
        userService.updatePassword(request);
        return ResponseEntity.ok("Password has been updated successfully.");
    }

    @PatchMapping("/addressUpdate")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    public ResponseEntity<String> updateAddress(@Valid @RequestBody AddressUpdateCO request) {
        userService.updateAddress(request);
        return ResponseEntity.ok("Address has been updated successfully.");
    }

}
