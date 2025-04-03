package com.ecommerce.application.entity;

import com.ecommerce.application.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private RoleEnum authority;

    @OneToMany(mappedBy = "roles", cascade = CascadeType.ALL)
    private List<User> users;

    public Role(RoleEnum authority) {
        this.authority = authority;
    }
}

