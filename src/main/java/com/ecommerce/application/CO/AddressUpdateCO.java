package com.ecommerce.application.CO;

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

    @Pattern(regexp = "^[0-9]{6}$", message = "Zip Code must be a 6-digit number")
    private String zipCode;
    private String label;
}
