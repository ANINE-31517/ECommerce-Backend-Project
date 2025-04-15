package com.ecommerce.application.CO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryMetadataFieldCO {

    @NotBlank(message = "Field name is required!")
    private String name;
}
