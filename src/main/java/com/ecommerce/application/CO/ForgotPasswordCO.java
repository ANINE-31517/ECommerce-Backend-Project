package com.ecommerce.application.CO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordCO {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
}

