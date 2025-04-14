package com.ecommerce.application.controller;

import com.ecommerce.application.CO.AddressUpdateCO;
import com.ecommerce.application.CO.UpdatePasswordCO;
import com.ecommerce.application.CO.UserLoginCO;
import com.ecommerce.application.VO.AddressVO;
import com.ecommerce.application.VO.TokenResponseVO;
import com.ecommerce.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MessageSource messageSource;

    @PutMapping("/updatePassword")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody UpdatePasswordCO request) {
        userService.updatePassword(request);
        String message = messageSource.getMessage("password.updated.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/addressUpdate")
    //@PreAuthorize("(hasAuthority('CUSTOMER') or hasAuthority('SELLER')) and @securityService.isValidUser(request.id, authentication)")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    public ResponseEntity<String> updateAddress(@Valid @RequestBody AddressUpdateCO request) {
        userService.updateAddress(request);
        String message = messageSource.getMessage("address.updated.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/getAddress")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    public ResponseEntity<List<AddressVO>> getAddresses() {
        List<AddressVO> addresses = userService.getAddresses();
        return ResponseEntity.ok(addresses);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseVO> loginCustomer(@Valid @RequestBody UserLoginCO request) {
        TokenResponseVO responseVO = userService.loginUser(request);
        return ResponseEntity.ok(responseVO);
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<String> logoutUser(@RequestHeader("Authorization") String request) {
        userService.logoutUser(request);
        String message = messageSource.getMessage("logout.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

}
