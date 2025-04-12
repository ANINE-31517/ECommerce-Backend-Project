package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressVO {

    private String city;
    private String state;
    private String country;
    private String addressLine;
    private String zipCode;
    private String label;
}
