package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class CategoryViewVO {

    private UUID id;
    private String name;
    private List<CategoryViewSummaryVO> parentHierarchy;
    private List<CategoryViewSummaryVO> children;
    private Map<String, Set<String>> metadataFields;
}
