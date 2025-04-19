package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProductViewVO {

    private UUID id;
    private String name;
    private String description;
    private String brand;
    private boolean active;
    private boolean cancellable;
    private boolean returnable;
    private CategoryViewSummaryVO category;
}
