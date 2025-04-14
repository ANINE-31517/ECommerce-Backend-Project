package com.ecommerce.application.CO;

import com.ecommerce.application.constant.RegexPatternConstant;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerProfileUpdateCO {

    private String firstName;
    private String lastName;

    @Pattern(regexp = RegexPatternConstant.CONTACT_NUMBER_PATTERN, message = "Customer contact must be a 10-digit number")
    private String contact;
}
