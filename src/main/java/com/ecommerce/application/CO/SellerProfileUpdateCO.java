package com.ecommerce.application.CO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SellerProfileUpdateCO {

    private String firstName;

    private String lastName;
    private String companyName;
    private String companyContact;
}
