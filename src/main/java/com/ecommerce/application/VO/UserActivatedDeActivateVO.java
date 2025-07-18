package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserActivatedDeActivateVO {

    private boolean isActivated;
    private String message;
}
