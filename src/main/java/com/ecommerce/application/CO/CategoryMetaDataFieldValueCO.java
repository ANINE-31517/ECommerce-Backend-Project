package com.ecommerce.application.CO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CategoryMetaDataFieldValueCO {

    @NotBlank
    private String categoryId;

    @NotEmpty
    @Valid
    private List<MetaDataFieldValueCO> fieldValues;
}
