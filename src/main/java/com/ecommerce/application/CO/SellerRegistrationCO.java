package com.ecommerce.application.CO;

import com.ecommerce.application.constant.RegexPatternConstant;
import com.ecommerce.application.entity.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class SellerRegistrationCO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Confirm Password is required")
    private String confirmPassword;

    @NotBlank(message = "GST is required")
    private String gst;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @Valid
    private AddressCO companyAddress;

    @NotBlank(message = "Company contact is required")
    @Pattern(regexp = RegexPatternConstant.CONTACT_NUMBER_PATTERN, message = "Company contact must be a 10-digit number")
    private String companyContact;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

}

