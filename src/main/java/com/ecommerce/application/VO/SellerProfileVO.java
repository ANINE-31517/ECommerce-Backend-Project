package com.ecommerce.application.VO;

import com.ecommerce.application.entity.Address;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SellerProfileVO {

    private UUID id;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private String companyName;
    private String companyContact;
    private String gst;
    private String image;
    private Address address;
}

