package com.ecommerce.application.constant;

import java.util.List;

public class CategoryConstant {

    private CategoryConstant() {}

    public static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "createdAt");
    public static final List<String> ALLOWED_ORDER_FIELDS = List.of("asc", "desc");
}
