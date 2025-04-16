package com.ecommerce.application.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class CategoryViewSummaryVO {

    private UUID id;
    private String name;
}
