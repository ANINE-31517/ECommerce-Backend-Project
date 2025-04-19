package com.ecommerce.application.controller;


import com.ecommerce.application.CO.ProductAddCO;
import com.ecommerce.application.VO.ProductViewVO;
import com.ecommerce.application.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final MessageSource messageSource;

    @PostMapping("/sellers/add-product")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<String> addProduct(@Valid @RequestBody ProductAddCO request) {
        productService.addProduct(request);
        String message = messageSource.getMessage("add.product.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/sellers/view-product/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<ProductViewVO> viewProduct(@PathVariable UUID id) {
        ProductViewVO response = productService.viewProduct(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sellers/view-all-product")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Page<ProductViewVO>> viewAllProduct(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query
    ) {
        Page<ProductViewVO> response = productService.viewAllProduct(offset, max, sort, order, query);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sellers/delete-product/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<String> deleteAddress(@PathVariable UUID id) {
        productService.deleteProduct(id);
        String message = messageSource.getMessage("product.delete.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }
}
