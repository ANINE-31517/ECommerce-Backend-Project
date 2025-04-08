package com.ecommerce.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String accessToken;

    private String refreshToken;

    private boolean accessTokenInvalidated;

    private boolean refreshTokenInvalidated;

    @ManyToOne
    private User user;
}
