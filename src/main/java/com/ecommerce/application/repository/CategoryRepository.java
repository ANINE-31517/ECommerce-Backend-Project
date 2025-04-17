package com.ecommerce.application.repository;

import com.ecommerce.application.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameAndParentCategoryIsNull(String name);
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
