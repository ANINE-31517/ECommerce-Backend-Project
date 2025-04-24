package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class CategoryFilterDetailsVO {
    private Map<String, Set<String>> metadataFields;
    private Set<String> brands;
    private Double minPrice;
    private Double maxPrice;
}
