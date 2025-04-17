package com.ecommerce.application.repository;

import com.ecommerce.application.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameAndParentCategoryIsNull(String name);
}
