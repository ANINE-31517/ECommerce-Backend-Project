package com.ecommerce.application.controller;

import com.ecommerce.application.VO.UserActivatedDeActivateVO;
import com.ecommerce.application.VO.CustomerRegisteredVO;
import com.ecommerce.application.VO.SellerRegisteredVO;
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

    private final CustomerService customerService;
    private final SellerService sellerService;

    @GetMapping("/get-customer")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<CustomerRegisteredVO>> getAllCustomers(
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "dateCreated") String sortBy,
            @RequestParam(required = false) String email
    ) {
        Page<CustomerRegisteredVO> customers = customerService.getAllCustomers(pageOffset, pageSize, sortBy, email);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/get-seller")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<SellerRegisteredVO>> getAllSellers(
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "dateCreated") String sortBy,
            @RequestParam(required = false) String email
    ) {
        Page<SellerRegisteredVO> sellers = sellerService.getAllSellers(pageOffset, pageSize, sortBy, email);
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping("/activate-customer/{customerId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserActivatedDeActivateVO> activateCustomer(@PathVariable UUID customerId) {
        UserActivatedDeActivateVO response = customerService.activateCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/activate-seller/{sellerId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserActivatedDeActivateVO> activateSeller(@PathVariable UUID sellerId) {
        UserActivatedDeActivateVO response = sellerService.activateSeller(sellerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/deActivate-customer/{customerId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserActivatedDeActivateVO> deActivateCustomer(@PathVariable UUID customerId) {
        UserActivatedDeActivateVO response = customerService.deActivateCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/deActivate-seller/{sellerId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserActivatedDeActivateVO> deActivateSeller(@PathVariable UUID sellerId) {
        UserActivatedDeActivateVO response = sellerService.deActivateSeller(sellerId);
        return ResponseEntity.ok(response);
    }

}
