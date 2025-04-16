package com.ecommerce.application.controller;

import com.ecommerce.application.CO.CategoryCO;
import com.ecommerce.application.CO.CategoryMetadataFieldCO;
import com.ecommerce.application.VO.CategoryMetaDataFieldListVO;
import com.ecommerce.application.VO.CategoryMetaDataFieldVO;
import com.ecommerce.application.VO.CategoryVO;
import com.ecommerce.application.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/admin/add-metadata-field")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CategoryMetaDataFieldVO> create(@Valid @RequestBody CategoryMetadataFieldCO request) {
        CategoryMetaDataFieldVO response = categoryService.createField(request.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all-metadata-field")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<CategoryMetaDataFieldListVO>> getAllMetadataFields(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query
    ) {
        Page<CategoryMetaDataFieldListVO> response = categoryService.getAllFields(offset, max, sort, order, query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/add-category")
    public ResponseEntity<CategoryVO> createCategory(@Valid @RequestBody CategoryCO request) {
        CategoryVO response = categoryService.addCategory(request);
        return ResponseEntity.ok(response);
    }

}
