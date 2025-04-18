package com.ecommerce.application.repository;

import com.ecommerce.application.entity.Category;
import com.ecommerce.application.entity.CategoryMetaDataField;
import com.ecommerce.application.entity.CategoryMetaDataFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryMetaDataFieldValueRepository extends JpaRepository<CategoryMetaDataFieldValue, UUID> {

    Optional<CategoryMetaDataFieldValue> findByCategoryAndCategoryMetaDataField(Category category, CategoryMetaDataField field);
}
