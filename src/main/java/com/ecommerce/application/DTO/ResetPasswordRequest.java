package com.ecommerce.application.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotNull(message = "Token is Required!")
    private String token;

    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter.")
    @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter.")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one number.")
    @Pattern(regexp = ".*[\\W_].*", message = "Password must contain at least one special character.")
    @NotNull(message = "Password is Required!")
    private String password;

    @NotNull(message = "Confirm Password is Required!")
    private String confirmPassword;
}

