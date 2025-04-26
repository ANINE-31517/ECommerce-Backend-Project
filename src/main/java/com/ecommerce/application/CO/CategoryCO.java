package com.ecommerce.application.CO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCO {

    @NotBlank(message = "Category name is required.")
    private String name;

    private String parentCategoryId;
}

