package com.ecommerce.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private float amountPaid;
    private LocalDateTime dateCreated;
    private String paymentMethod;

    private String customerAddressCity;
    private String customerAddressState;
    private String customerAddressCountry;
    private String customerAddressAddressLine;
    private String customerAddressZipCode;
    private String customerAddressLabel;

    @ManyToOne
    @JoinColumn(name = "customer_user_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts;
}

