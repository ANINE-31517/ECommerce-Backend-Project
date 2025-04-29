package com.ecommerce.application.repository;

import com.ecommerce.application.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByAccessToken(String accessToken);
    Optional<Token> findByRefreshToken(String accessToken);

    @Modifying
    @Query("delete from Token t where t.lastUpdated < :cutoffTime")
    void deleteByLastUpdatedBefore(LocalDateTime cutoffTime);
}

