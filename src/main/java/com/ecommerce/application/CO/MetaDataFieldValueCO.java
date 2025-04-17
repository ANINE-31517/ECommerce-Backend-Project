package com.ecommerce.application.CO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MetaDataFieldValueCO {

    @NotBlank
    private String fieldId;

    @NotEmpty
    private List<@NotBlank String> values;
}
