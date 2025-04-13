package com.ecommerce.application.controller;

import com.ecommerce.application.VO.UserActivatedDeActivateVO;
import com.ecommerce.application.VO.CustomerRegisteredVO;
import com.ecommerce.application.VO.SellerRegisteredVO;
import com.ecommerce.application.service.AdminService;
import com.ecommerce.application.service.CustomerService;
import com.ecommerce.application.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CustomerService customerService;
    private final SellerService sellerService;

//    @PostMapping("/login")
//    public ResponseEntity<TokenResponseVO> loginAdmin(@Valid @RequestBody AdminLoginCO request) {
//        TokenResponseVO responseVO = adminService.loginAdmin(request);
//        return ResponseEntity.ok(responseVO);
//    }

//    @PostMapping("/logout")
//    @PreAuthorize("hasAuthority('ADMIN')")
//    public ResponseEntity<String> logoutAdmin(@RequestHeader("Authorization") String request) {
//        adminService.logoutAdmin(request);
//        return ResponseEntity.ok("Logout successful. Access token is now invalidated.");
//    }


    @GetMapping("/getCustomer")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<CustomerRegisteredVO>> getAllCustomers(
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String email
    ) {
        Page<CustomerRegisteredVO> customers = customerService.getAllCustomers(pageOffset, pageSize, sortBy, email);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/getSeller")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<SellerRegisteredVO>> getAllSellers(
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String email
    ) {
        Page<SellerRegisteredVO> sellers = sellerService.getAllSellers(pageOffset, pageSize, sortBy, email);
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping("/activateCustomer/{customerId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserActivatedDeActivateVO> activateCustomer(@PathVariable UUID customerId) {
        UserActivatedDeActivateVO response = customerService.activateCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/activateSeller/{sellerId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserActivatedDeActivateVO> activateSeller(@PathVariable UUID sellerId) {
        UserActivatedDeActivateVO response = sellerService.activateSeller(sellerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/deActivateCustomer/{customerId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserActivatedDeActivateVO> deActivateCustomer(@PathVariable UUID customerId) {
        UserActivatedDeActivateVO response = customerService.deActivateCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/deActivateSeller/{sellerId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserActivatedDeActivateVO> deActivateSeller(@PathVariable UUID sellerId) {
        UserActivatedDeActivateVO response = sellerService.deActivateSeller(sellerId);
        return ResponseEntity.ok(response);
    }

}
