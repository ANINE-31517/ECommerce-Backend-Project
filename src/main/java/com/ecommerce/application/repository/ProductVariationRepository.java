package com.ecommerce.application.repository;

import com.ecommerce.application.entity.Product;
import com.ecommerce.application.entity.ProductVariation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductVariationRepository extends JpaRepository<ProductVariation, UUID> {

    List<ProductVariation> findByProduct(Product product);
    Page<ProductVariation> findAllByProduct(Pageable pageable, Product product);
    Page<ProductVariation> findAllByMetadataContainingIgnoreCaseAndProduct(String query, Product product, Pageable pageable);
}
