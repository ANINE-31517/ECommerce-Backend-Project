package com.ecommerce.application.CO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductAddCO {

    @NotBlank
    private String name;

    @NotBlank
    private String brand;

    @NotBlank
    private String categoryId;

    private String description;
    private Boolean cancellable = false;
    private Boolean returnable = false;
}
