package com.ecommerce.application.entity;

import com.ecommerce.application.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_variation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantityAvailable;
    private float price;
    private String primaryImageName;
    private String metadata;
    private boolean isActive;
    private int imageCount;

    @OneToMany(mappedBy = "productVariation", cascade = CascadeType.ALL)
    private List<Cart> cartList;
}
