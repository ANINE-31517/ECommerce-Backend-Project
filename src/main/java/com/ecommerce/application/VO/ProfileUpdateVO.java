package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileUpdateVO {

    private boolean success;
    private String message;
}
