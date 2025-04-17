package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class CategoryViewAllVO {

    private UUID id;
    private String name;
    private Map<String, Set<String>> metadataFields;
}
