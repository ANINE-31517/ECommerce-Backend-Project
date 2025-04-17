package com.ecommerce.application.repository;

import com.ecommerce.application.entity.CategoryMetaDataFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryMetaDataFieldValueRepository extends JpaRepository<CategoryMetaDataFieldValue, UUID> {

    boolean existsByFieldValues(String fieldValues);
}
