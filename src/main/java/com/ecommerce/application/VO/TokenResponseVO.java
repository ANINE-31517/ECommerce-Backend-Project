package com.ecommerce.application.VO;

import lombok.*;

@Data
@Builder
public class TokenResponseVO {

    private String accessToken;
    private String refreshToken;
}