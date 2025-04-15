package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CategoryMetadataFieldVO {

    private String message;
    private UUID fieldId;
}

