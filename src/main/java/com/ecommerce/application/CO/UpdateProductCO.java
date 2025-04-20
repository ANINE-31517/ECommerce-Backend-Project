package com.ecommerce.application.CO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProductCO {

    @NotBlank(message = "Product Id name is required.")
    private String id;

    private String name;
    private String description;
    private Boolean cancellable;
    private Boolean returnable;
}
