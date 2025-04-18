package com.ecommerce.application.repository;

import com.ecommerce.application.entity.CategoryMetaDataFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryMetaDataFieldValueRepository extends JpaRepository<CategoryMetaDataFieldValue, UUID> {

    boolean existsByFieldValues(String fieldValues);

    Optional<CategoryMetaDataFieldValue> findByCategoryIdAndCategoryMetaDataFieldId(UUID categoryId, UUID fieldId);
}
