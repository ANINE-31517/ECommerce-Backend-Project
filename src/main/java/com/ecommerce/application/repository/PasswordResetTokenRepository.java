package com.ecommerce.application.repository;

import com.ecommerce.application.entity.PasswordResetToken;
import com.ecommerce.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @Modifying
    @Query("delete from PasswordResetToken p where p.user = :user")
    void deleteByUser(@Param("user") User user);

    Optional<PasswordResetToken> findByToken(String token);
}
