package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CustomerProductViewVO {

    private UUID productId;
    private String name;
    private String brand;
    private boolean active;

    private CategoryViewSummaryVO category;

    private List<CustomerProductVariationViewVO> productVariations;
}
