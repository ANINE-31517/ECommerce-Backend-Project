package com.ecommerce.application.CO;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerProfileUpdateCO {

    private String firstName;
    private String lastName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Customer contact must be a 10-digit number")
    private String contact;
}
