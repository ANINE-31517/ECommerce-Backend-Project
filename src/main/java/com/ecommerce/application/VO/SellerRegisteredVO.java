package com.ecommerce.application.VO;

import com.ecommerce.application.entity.Address;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SellerRegisteredVO {

    private UUID id;
    private String fullName;
    private String email;
    private boolean isActive;
    private String companyName;
    private String companyContact;
    private Address companyAddress;
}
