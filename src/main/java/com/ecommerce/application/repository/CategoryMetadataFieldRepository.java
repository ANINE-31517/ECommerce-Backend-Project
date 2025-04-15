package com.ecommerce.application.repository;

import com.ecommerce.application.entity.CategoryMetadataField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryMetadataFieldRepository extends JpaRepository<CategoryMetadataField, UUID> {
    boolean existsByNameIgnoreCase(String name);
}

