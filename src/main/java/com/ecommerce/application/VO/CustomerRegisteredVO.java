package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CustomerRegisteredVO {
    private UUID id;
    private String fullName;
    private String email;
    private boolean isActive;
}
