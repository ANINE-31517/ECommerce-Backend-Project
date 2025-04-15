package com.ecommerce.application.entity;

import com.ecommerce.application.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private RoleEnum authority;

    @OneToMany(mappedBy = "roles", cascade = CascadeType.ALL)
    private List<User> users;

    public Role(RoleEnum authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority.name();
    }
}



