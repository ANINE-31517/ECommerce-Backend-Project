package com.ecommerce.application.repository;

import com.ecommerce.application.entity.Category;
import com.ecommerce.application.entity.Product;
import com.ecommerce.application.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByNameAndBrandAndCategoryAndSeller(String name, String brand, Category category, Seller seller);
    Optional<Product> findByIdAndSellerIdAndIsDeletedFalse(UUID productId, UUID sellerId);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findAllByCategoryIdOrSellerId(UUID categoryId, UUID sellerId, Pageable pageable);
    Page<Product> findAllByCategoryAndIsActiveTrueAndIsDeletedFalseAndNameContainingIgnoreCase(Category category, String name, Pageable pageable);
    Page<Product> findAllByCategoryAndIsActiveTrueAndIsDeletedFalse(Category category, Pageable pageable);
    Page<Product> findAllByCategoryAndBrandAndIsActiveTrueAndIsDeletedFalseAndIdNotAndNameContainingIgnoreCase(Category category, String brand, UUID id, String name, Pageable pageable);
    Page<Product> findAllByCategoryAndBrandAndIsActiveTrueAndIsDeletedFalseAndIdNot(Category category, String brand, UUID id, Pageable pageable);
}
