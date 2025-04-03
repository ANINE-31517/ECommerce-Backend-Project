package com.ecommerce.application.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String email;

    private String firstName;
    private String middleName;
    private String lastName;

    private String password;
    private boolean isDeleted = false;
    private boolean isActive = true;
    private boolean isExpired = false;
    private boolean isLocked = false;
    private int invalidAttemptCount = 0;
    private LocalDateTime passwordUpdateDate;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "role_id")
    private Role roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses;

}
