package com.ecommerce.application.CO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SellerProfileUpdateCO {

    private String firstName;

    private String lastName;
    private String companyName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Company contact must be a 10-digit number")
    private String companyContact;
}
