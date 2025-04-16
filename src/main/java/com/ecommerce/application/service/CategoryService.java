package com.ecommerce.application.service;

import com.ecommerce.application.CO.CategoryCO;
import com.ecommerce.application.VO.CategoryMetaDataFieldListVO;
import com.ecommerce.application.VO.CategoryMetaDataFieldVO;
import com.ecommerce.application.VO.CategoryVO;
import com.ecommerce.application.constant.CategoryConstant;
import com.ecommerce.application.entity.Category;
import com.ecommerce.application.entity.CategoryMetaDataField;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.repository.CategoryMetadataFieldRepository;
import com.ecommerce.application.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryMetadataFieldRepository categoryMetadataFieldRepository;
    private final CategoryRepository categoryRepository;

    private static final List<String> allowedSortFields = CategoryConstant.ALLOWED_SORT_FIELDS;
    private static final List<String> allowedOrderFields = CategoryConstant.ALLOWED_ORDER_FIELDS;

    public CategoryMetaDataFieldVO createField(String name) {

        if (categoryMetadataFieldRepository.existsByNameIgnoreCase(name)) {
            log.warn("Field name {} is not unique", name);
            throw new BadRequestException("Field name must be unique");
        }

        CategoryMetaDataField field = new CategoryMetaDataField();
        field.setName(name);
        field.setCreatedAt(LocalDateTime.now());

        categoryMetadataFieldRepository.save(field);
        log.info("New metadata field created with name: {}", name);

        return CategoryMetaDataFieldVO.builder()
                .message("Metadata field created successfully!")
                .fieldId(field.getId())
                .build();
    }

    public Page<CategoryMetaDataFieldListVO> getAllFields(int offset, int max, String sort, String order, String query) {

        if (!allowedSortFields.contains(sort)) {
            log.error("Invalid sort type is passed, choose among (name, createdAt)");
            throw new BadRequestException("Only 'name' and 'createdAt' are allowed in sort field.");
        }

        if (!allowedOrderFields.contains(order)) {
            log.error("Invalid order type is passed, choose among (asc, desc)");
            throw new BadRequestException("Only 'asc' and 'desc' are allowed in order field.");
        }

        Sort sortOrder = order.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();

        Pageable pageable = PageRequest.of(offset, max, sortOrder);

        Page<CategoryMetaDataField> page;

        if (query != null && !query.isBlank()) {
            page = categoryMetadataFieldRepository.findByNameContainingIgnoreCase(query, pageable);
        } else {
            page = categoryMetadataFieldRepository.findAll(pageable);
        }
        log.info("Total fields fetched: {}", page.getTotalElements());

        return page.map(this::convertToCategoryMetaDataFieldListVO);
    }

    private CategoryMetaDataFieldListVO convertToCategoryMetaDataFieldListVO(CategoryMetaDataField categoryMetaDataField) {

        return CategoryMetaDataFieldListVO.builder()
                .name(categoryMetaDataField.getName())
                .id(categoryMetaDataField.getId())
                .build();
    }

    public CategoryVO addCategory(CategoryCO request) {
        String parentCategoryId = request.getParentCategoryId();

        Category parentCategory = null;
        UUID parentId = null;
        if (parentCategoryId != null) {
            try {
                parentId = UUID.fromString(parentCategoryId);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid UUID for parentCategoryId!");
            }

            parentCategory = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new BadRequestException("Parent category does not found!"));
        }

        if (!isNameUniqueInHierarchy(request.getName(), parentCategory)) {
            log.error("Category name '{}' already exists in the hierarchy!", request.getName());
            throw new BadRequestException("Category name must be unique in the hierarchy!");
        }

        if (parentCategory != null && parentHasProducts(parentCategory)) {
            log.error("Parent with Id {} already has products!", parentId);
            throw new BadRequestException("Cannot add a sub-category to a category that already has products!");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setParentCategory(parentCategory);

        categoryRepository.save(category);
        log.info("Category Created: {} under Parent ID: {}", category.getName(), parentId);

        return CategoryVO.builder()
                .message("Category created successfully!")
                .categoryId(category.getId())
                .build();
    }

    private boolean isNameUniqueInHierarchy(String name, Category parentCategory) {

        if (parentCategory == null) {
            return !categoryRepository.existsByNameAndParentCategoryIsNull(name);
        }

        Category root = parentCategory;
        while (root.getParentCategory() != null) {
            root = root.getParentCategory();
        }

        Queue<Category> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Category current = queue.poll();

            if (current.getName().equalsIgnoreCase(name)) {
                return false;
            }

            List<Category> children = current.getSubCategories();
            if (children != null) {
                queue.addAll(children);
            }
        }
        return true;
    }

    private boolean parentHasProducts(Category parentCategory) {
        return parentCategory.getProducts() != null && !parentCategory.getProducts().isEmpty();
    }

}
