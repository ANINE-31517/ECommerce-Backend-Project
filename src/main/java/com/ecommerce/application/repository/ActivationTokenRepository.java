package com.ecommerce.application.repository;

import com.ecommerce.application.entity.ActivationToken;
import com.ecommerce.application.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, UUID> {
    Optional<ActivationToken> findByToken(String token);

    @Modifying
    @Query("delete from ActivationToken t where t.customer = :customer")
    void deleteByCustomer(@Param("customer") Customer customer);
}

