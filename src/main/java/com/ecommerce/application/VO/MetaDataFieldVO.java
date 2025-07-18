package com.ecommerce.application.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MetaDataFieldVO {

    private UUID id;
    private String name;
    private List<String> values;
}
