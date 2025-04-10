package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CustomerProfileVO {

    private UUID id;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private String contact;
    private String image;
}
