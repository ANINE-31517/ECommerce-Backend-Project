package com.ecommerce.application.CO;

import com.ecommerce.application.constant.RegexPatternConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerRegistrationCO {

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = RegexPatternConstant.CONTACT_NUMBER_PATTERN, message = "Invalid phone number")
    private String contact;

    @Pattern(regexp = RegexPatternConstant.PASSWORD_UPPERCASE, message = "Password must contain at least one uppercase letter.")
    @Pattern(regexp = RegexPatternConstant.PASSWORD_LOWERCASE, message = "Password must contain at least one lowercase letter.")
    @Pattern(regexp = RegexPatternConstant.PASSWORD_DIGIT, message = "Password must contain at least one number.")
    @Pattern(regexp = RegexPatternConstant.PASSWORD_SPECIAL_CHAR, message = "Password must contain at least one special character.")
    @NotBlank(message = "Password is mandatory")
    private String password;

    @NotBlank(message = "Confirm Password is mandatory")
    private String confirmPassword;

    @NotBlank(message = "First name is mandatory")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    private String lastName;

}


