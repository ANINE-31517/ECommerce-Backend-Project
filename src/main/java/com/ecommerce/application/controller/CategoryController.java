package com.ecommerce.application.controller;

import com.ecommerce.application.CO.*;
import com.ecommerce.application.VO.*;
import com.ecommerce.application.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final MessageSource messageSource;

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
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CategoryVO> createCategory(@Valid @RequestBody CategoryCO request) {
        CategoryVO response = categoryService.addCategory(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/view-category/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CategoryViewVO> viewCategory(@PathVariable String id) {
        CategoryViewVO response = categoryService.viewCategory(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/view-all-category")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<CategoryViewVO>> viewAllCategories(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query
    ) {
        Page<CategoryViewVO> categories = categoryService.viewAllCategory(offset, max, sort, order, query);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/admin/update-category")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CategoryVO> updateCategory(@Valid @RequestBody UpdateCategoryCO request) {
        CategoryVO response = categoryService.updateCategory(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/add-metadata-field-value")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> addMetaDataFieldValue(@Valid @RequestBody CategoryMetaDataFieldValueCO request) {
        categoryService.addMetaDataFieldValue(request);
        String message = messageSource.getMessage("category.meta.data.field.value.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @PutMapping("/admin/update-metadata-field-value")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> updateMetaDataFieldValues(@RequestBody @Valid CategoryMetaDataFieldValueCO request) {
        categoryService.updateMetaDataFieldValues(request);
        String message = messageSource.getMessage("update.meta.data.field.value.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/sellers/view-all-category")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<List<SellerCategoryViewSummaryVO>> viewAllSellerCategory() {
        List<SellerCategoryViewSummaryVO> response = categoryService.viewAllSellerCategory();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/view-all-category")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<List<CategoryViewSummaryVO>> viewAllCustomerCategory(@RequestParam(required = false) String categoryId) {
        List<CategoryViewSummaryVO> response = categoryService.viewAllCustomerCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/filtering-details/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CategoryFilterDetailsVO> getCategoryFilteringDetails(@PathVariable UUID id) {
        CategoryFilterDetailsVO response = categoryService.getFilteringDetails(id);
        return ResponseEntity.ok(response);
    }

}
