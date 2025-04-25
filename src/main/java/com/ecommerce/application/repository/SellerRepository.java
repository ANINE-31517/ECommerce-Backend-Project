package com.ecommerce.application.repository;

import com.ecommerce.application.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerRepository extends JpaRepository<Seller, UUID> {
    Optional<Seller> findByGst(String gst);
    Optional<Seller> findByCompanyName(String companyName);
    boolean existsByCompanyName(String companyName);
    Page<Seller> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
