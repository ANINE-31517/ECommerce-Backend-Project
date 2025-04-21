package com.ecommerce.application.CO;

import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class ProductVariationAddCO {

    @NotNull(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be 0 or more")
    private Integer quantityAvailable;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private Float price;

    @NotNull(message = "Metadata is required")
    @Size(min = 1, message = "At least one metadata field is required")
    private String metadata;
}

