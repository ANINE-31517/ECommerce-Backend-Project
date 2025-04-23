package com.ecommerce.application.repository;

import com.ecommerce.application.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameAndParentCategoryIsNull(String name);
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("select c from Category c where c.subCategories is empty")
    List<Category> findLeafCategories();

    List<Category> findByParentCategoryIsNull();
    List<Category> findByParentCategory(Category category);

    @Query("select count(c) > 0 from Category c where c.parentCategory.id = :categoryId")
    boolean hasSubCategories(@Param("categoryId") UUID categoryId);
}
