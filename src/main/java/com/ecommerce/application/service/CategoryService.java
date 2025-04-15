package com.ecommerce.application.service;

import com.ecommerce.application.VO.CategoryMetadataFieldVO;
import com.ecommerce.application.entity.CategoryMetadataField;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.repository.CategoryMetadataFieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryMetadataFieldRepository categoryMetadataFieldRepository;

    public CategoryMetadataFieldVO createField(String name) {

        if (categoryMetadataFieldRepository.existsByNameIgnoreCase(name)) {
            log.warn("Field name {} is not unique", name);
            throw new BadRequestException("Field name must be unique");
        }

        CategoryMetadataField field = new CategoryMetadataField();
        field.setName(name);

        categoryMetadataFieldRepository.save(field);
        log.info("New metadata field created with name: {}", name);

        return CategoryMetadataFieldVO.builder()
                .message("Metadata field created successfully!")
                .fieldId(field.getId())
                .build();
    }
}
