package com.ecommerce.application.entity;

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
@Table(name = "category_metadata_field")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMetaDataField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    private String name;

    @OneToMany(mappedBy = "categoryMetaDataField")
    private List<CategoryMetaDataFieldValue> categoryMetaDataFieldValues;
}

