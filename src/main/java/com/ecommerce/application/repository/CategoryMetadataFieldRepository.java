package com.ecommerce.application.repository;

import com.ecommerce.application.entity.CategoryMetaDataField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryMetadataFieldRepository extends JpaRepository<CategoryMetaDataField, UUID> {
    boolean existsByNameIgnoreCase(String name);
    Page<CategoryMetaDataField> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

