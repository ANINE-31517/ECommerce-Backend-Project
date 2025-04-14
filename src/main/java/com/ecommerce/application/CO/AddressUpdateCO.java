package com.ecommerce.application.CO;

import com.ecommerce.application.constant.RegexPatternConstant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class AddressUpdateCO {

    @NotNull(message = "Address ID is required")
    private UUID id;

    private String city;
    private String state;
    private String country;
    private String addressLine;

    @Pattern(regexp = RegexPatternConstant.ZIP_CODE_PATTERN, message = "Zip Code must be a 6-digit number")
    private String zipCode;
    private String label;
}
