package com.ecommerce.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    private int quantity;
    private boolean isWishlistItem;

    @ManyToOne
    @JoinColumn(name = "product_variation_id", nullable = false)
    private ProductVariation productVariation;

    @OneToOne
    @JoinColumn(name = "customer_user_id")
    private Customer customer;
}

