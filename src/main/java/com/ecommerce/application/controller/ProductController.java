package com.ecommerce.application.controller;

//import com.ecommerce.application.CO.CategoryCO;
//import com.ecommerce.application.VO.CategoryVO;
//import com.ecommerce.application.entity.Product;
//import com.ecommerce.application.service.ProductService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequestMapping("/api/product")
//@RequiredArgsConstructor
//public class ProductController {
//
//    private final ProductService productService;
//
//    @PostMapping("/sellers/add-product")
//    @PreAuthorize("hasAuthority('SELLER')")
//    public ResponseEntity<ProductVO> addProduct(@Valid @RequestBody ProductCO request) {
//        ProductVO response = productService.addProduct(request);
//        return ResponseEntity.ok(response);
//    }
//}
