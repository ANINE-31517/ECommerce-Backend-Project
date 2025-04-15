package com.ecommerce.application.controller;

import com.ecommerce.application.CO.CategoryMetadataFieldCO;
import com.ecommerce.application.VO.CategoryMetadataFieldVO;
import com.ecommerce.application.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/admin/add-category")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CategoryMetadataFieldVO> create(@Valid @RequestBody CategoryMetadataFieldCO request) {
        CategoryMetadataFieldVO response = categoryService.createField(request.getName());
        return ResponseEntity.ok(response);
    }

}
