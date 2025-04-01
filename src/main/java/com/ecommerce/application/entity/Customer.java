package com.ecommerce.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String contact;

    @OneToOne(mappedBy = "customer")
    private Cart cart;

//    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
//    private List<Orders> orders;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<ProductReview> productReviews;
}
