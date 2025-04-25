package com.ecommerce.application.CO;

import com.ecommerce.application.constant.RegexPatternConstant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordCO {

    @NotNull(message = "Token is Required!")
    private String token;

    @Pattern(regexp = RegexPatternConstant.PASSWORD_UPPERCASE, message = "Password must contain at least one uppercase letter.")
    @Pattern(regexp = RegexPatternConstant.PASSWORD_LOWERCASE, message = "Password must contain at least one lowercase letter.")
    @Pattern(regexp = RegexPatternConstant.PASSWORD_DIGIT, message = "Password must contain at least one number.")
    @Pattern(regexp = RegexPatternConstant.PASSWORD_SPECIAL_CHAR, message = "Password must contain at least one special character.")
    @Pattern(regexp = RegexPatternConstant.PASSWORD_LENGTH, message = "Password length should be between 8 to 15 characters long.")
    @NotNull(message = "Password is Required!")
    private String password;

    @NotNull(message = "Confirm Password is Required!")
    private String confirmPassword;
}

