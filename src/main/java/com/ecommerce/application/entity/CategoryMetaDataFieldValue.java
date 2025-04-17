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
@Table(name = "category_metadata_field_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMetaDataFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "category_metaData_field_id")
    private CategoryMetaDataField categoryMetaDataField;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String fieldValues;
}

