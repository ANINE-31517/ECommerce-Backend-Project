package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CustomerProductVariationViewVO {

    private UUID id;
    private int quantityAvailable;
    private float price;
    private String primaryImage;
    private String metadata;
    private boolean isActive;
    private List<String> secondaryImages;
}
