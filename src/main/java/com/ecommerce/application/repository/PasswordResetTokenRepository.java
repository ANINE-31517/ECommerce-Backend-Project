package com.ecommerce.application.repository;

import com.ecommerce.application.entity.PasswordResetToken;
import com.ecommerce.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    void deleteByUser(User user);
    Optional<PasswordResetToken> findByToken(String token);
}
