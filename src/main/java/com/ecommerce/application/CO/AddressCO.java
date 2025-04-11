package com.ecommerce.application.CO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressCO {

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Address Line is required")
    private String addressLine;

    @NotBlank(message = "Zip Code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Zip Code must be a 6-digit number")
    private String zipCode;

    private String label;
}
