package com.ecommerce.application.CO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenCO {

    @NotBlank(message = "Refresh token is required!")
    private String refreshToken;
}
