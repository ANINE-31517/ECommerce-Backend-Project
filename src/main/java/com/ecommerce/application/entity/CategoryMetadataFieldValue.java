package com.ecommerce.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "category_metadata_field_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMetadataFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "category_metadata_field_id")
    private CategoryMetadataField categoryMetadataField;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String fieldValues;
}

