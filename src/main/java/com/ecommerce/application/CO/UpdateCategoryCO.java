package com.ecommerce.application.CO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCategoryCO {

    @NotBlank(message = "Category name is required.")
    private String name;

    @NotBlank(message = "Category ID is required.")
    private String categoryId;
}
