package com.ecommerce.application.service;

import com.ecommerce.application.CO.CategoryCO;
import com.ecommerce.application.CO.CategoryMetaDataFieldValueCO;
import com.ecommerce.application.CO.MetaDataFieldValueCO;
import com.ecommerce.application.CO.UpdateCategoryCO;
import com.ecommerce.application.VO.*;
import com.ecommerce.application.constant.CategoryConstant;
import com.ecommerce.application.entity.Category;
import com.ecommerce.application.entity.CategoryMetaDataField;
import com.ecommerce.application.entity.CategoryMetaDataFieldValue;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.repository.CategoryMetaDataFieldRepository;
import com.ecommerce.application.repository.CategoryMetaDataFieldValueRepository;
import com.ecommerce.application.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryMetaDataFieldRepository categoryMetadataFieldRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMetaDataFieldValueRepository categoryMetaDataFieldValueRepository;


    private static final List<String> allowedSortFields = CategoryConstant.ALLOWED_SORT_FIELDS;
    private static final List<String> allowedOrderFields = CategoryConstant.ALLOWED_ORDER_FIELDS;

    public CategoryMetaDataFieldVO createField(String name) {

        if (categoryMetadataFieldRepository.existsByNameIgnoreCase(name)) {
            log.warn("Field name {} is not unique", name);
            throw new BadRequestException("Field name must be unique");
        }

        CategoryMetaDataField field = new CategoryMetaDataField();
        field.setName(name);

        categoryMetadataFieldRepository.save(field);
        log.info("New metadata field created with name: {}", name);

        return CategoryMetaDataFieldVO.builder()
                .message("Metadata field created successfully!")
                .fieldId(field.getId())
                .build();
    }

    public Page<CategoryMetaDataFieldListVO> getAllFields(int offset, int max, String sort, String order, String query) {

        if (!allowedSortFields.contains(sort)) {
            log.error("Invalid sort type is passed, choose among (name, dateCreated)");
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

    private boolean isNameUniqueInHierarchy(String name, Category category) {

        if (category == null) {
            return !categoryRepository.existsByNameAndParentCategoryIsNull(name);
        }

        Category root = category;
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

    private boolean parentHasProducts(Category category) {
        return category.getProducts() != null && !category.getProducts().isEmpty();
    }

    public CategoryViewVO viewCategory(String id) {
        UUID categoryId = null;
        try {
            categoryId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid Category ID format!");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

        List<CategoryViewSummaryVO> parentHierarchy = new ArrayList<>();
        Category current = category.getParentCategory();
        while (current != null) {
            parentHierarchy.addFirst(new CategoryViewSummaryVO(current.getId(), current.getName()));
            current = current.getParentCategory();
        }

        List<CategoryViewSummaryVO> children = new ArrayList<>();
        for (Category sub : category.getSubCategories()) {
            children.add(new CategoryViewSummaryVO(sub.getId(), sub.getName()));
        }

        Map<String, Set<String>> metadataMap = new HashMap<>();

        for (CategoryMetaDataFieldValue meta : category.getCategoryMetaDataFieldValues()) {
            String fieldName = meta.getCategoryMetaDataField().getName();
            List<String> values = Arrays.asList(meta.getFieldValues().split(","));

            if (!metadataMap.containsKey(fieldName)) {
                metadataMap.put(fieldName, new HashSet<>());
            }
            metadataMap.get(fieldName).addAll(values);
        }

        return CategoryViewVO.builder()
                .id(category.getId())
                .name(category.getName())
                .parentHierarchy(parentHierarchy)
                .children(children)
                .metadataFields(metadataMap)
                .build();
    }

    public Page<CategoryViewVO> viewAllCategory(int offset, int max, String sort, String order, String query) {

        if (!allowedSortFields.contains(sort)) {
            log.error("Invalid sort type passed, choose among (name, dateCreated)");
            throw new BadRequestException("Only 'name' and 'dateCreated' are allowed in sort field.");
        }

        if (!allowedOrderFields.contains(order)) {
            log.error("Invalid order type is passed, choose among (asc or desc)");
            throw new BadRequestException("Only 'asc' and 'desc' are allowed in order field.");
        }

        Sort sortOrder = order.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();

        Pageable pageable = PageRequest.of(offset, max, sortOrder);

        Page<Category> allCategory;
        if (query != null && !query.isBlank()) {
            allCategory = categoryRepository.findByNameContainingIgnoreCase(query, pageable);
        } else {
            allCategory = categoryRepository.findAll(pageable);
        }
        log.info("Total categories fetched: {}", allCategory.getTotalElements());

        return allCategory.map(category -> this.viewCategory(category.getId().toString()));
    }



    public CategoryVO updateCategory(UpdateCategoryCO request) {
        String name = request.getName().trim();
        String categoryCOId = request.getCategoryId();

        Category category = null;
        UUID categoryId = null;
        try {
            categoryId = UUID.fromString(categoryCOId);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid UUID for parentCategoryId!");
        }

        category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException("Category does not found!"));

        if (name.equalsIgnoreCase(category.getName())) {
            throw new BadRequestException("Same name provided!");
        }

        if (!isNameUniqueInHierarchy(name, category)) {
            log.error("Category name '{}' already present in the hierarchy!", request.getName());
            throw new BadRequestException("Category name must be unique in the hierarchy!");
        }

        if (parentHasProducts(category)) {
            log.error("Category with Id {} already has products!", categoryId);
            throw new BadRequestException("Cannot add a sub-category to a category that already has products!");
        }

        category.setName(name);

        categoryRepository.save(category);
        log.info("Category Updated with name: {}", name);

        return CategoryVO.builder()
                .message("Category updated successfully!")
                .categoryId(categoryId)
                .build();
    }

    public void addMetaDataFieldValue(CategoryMetaDataFieldValueCO request) {
        UUID categoryId;
        try {
            categoryId = UUID.fromString(request.getCategoryId());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid Category ID format");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            throw new BadRequestException("Metadata can only be added to a leaf category (no subcategories)");
        }

        for (MetaDataFieldValueCO fieldValueCO : request.getFieldValues()) {
            UUID fieldId;
            try {
                fieldId = UUID.fromString(fieldValueCO.getFieldId());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid Metadata Field ID format");
            }

            CategoryMetaDataField field = categoryMetadataFieldRepository.findById(fieldId)
                    .orElseThrow(() -> new ResourceNotFoundException("Metadata Field not found"));

            Set<String> uniqueValues = new HashSet<>(fieldValueCO.getValues());
            if (uniqueValues.size() != fieldValueCO.getValues().size()) {
                throw new BadRequestException("Duplicate values found for metadata field: " + field.getName());
            }

            String joinUniqueValues = String.join(", ", uniqueValues);

            if (categoryMetaDataFieldValueRepository.existsByFieldValues(joinUniqueValues)) {
                throw new BadRequestException("Same field value already exists!");
            }

            CategoryMetaDataFieldValue fieldValue = new CategoryMetaDataFieldValue();
            fieldValue.setCategory(category);
            fieldValue.setCategoryMetaDataField(field);
            fieldValue.setFieldValues(joinUniqueValues);

            categoryMetaDataFieldValueRepository.save(fieldValue);
        }
    }


}
