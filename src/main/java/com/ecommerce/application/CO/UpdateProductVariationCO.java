package com.ecommerce.application.CO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


@Data
public class UpdateProductVariationCO {

    @NotNull(message = "Product variation ID is required")
    private String productVariationId;

    @Positive(message = "Quantity must be 0 or more")
    private Integer quantityAvailable;

    @DecimalMin(value = "0.0", inclusive = true)
    private Float price;

    private String metadata;
    private Boolean isActive;
}
