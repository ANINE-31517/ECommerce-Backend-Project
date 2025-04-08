package com.ecommerce.application.repository;

import com.ecommerce.application.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByAccessToken(String accessToken);
    Optional<Token> findByRefreshToken(String accessToken);
}

