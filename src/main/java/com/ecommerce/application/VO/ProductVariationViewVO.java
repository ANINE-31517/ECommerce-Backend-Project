package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ProductVariationViewVO {

    private UUID id;
    private int quantityAvailable;
    private float price;
    private String primaryImageName;
    private String metadata;
    private boolean isActive;
    private ProductViewVO productViewVO;
}
