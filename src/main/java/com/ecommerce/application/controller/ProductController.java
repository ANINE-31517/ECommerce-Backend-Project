package com.ecommerce.application.controller;


import com.ecommerce.application.CO.ProductAddCO;
import com.ecommerce.application.VO.ProductViewVO;
import com.ecommerce.application.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ProductViewVO> viewProduct(@PathVariable String id) {
        ProductViewVO response = productService.viewProduct(id);
        return ResponseEntity.ok(response);
    }
}
