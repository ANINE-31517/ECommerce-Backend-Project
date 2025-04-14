package com.ecommerce.application.CO;

import com.ecommerce.application.constant.RegexPatternConstant;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SellerProfileUpdateCO {

    private String firstName;

    private String lastName;
    private String companyName;

    @Pattern(regexp = RegexPatternConstant.CONTACT_NUMBER_PATTERN, message = "Company contact must be a 10-digit number")
    private String companyContact;
}
