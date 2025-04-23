package com.ecommerce.application.constant;

import java.util.List;

public class CategoryConstant {

    private CategoryConstant() {}

    public static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "dateCreated", "price", "brand");
    public static final List<String> ALLOWED_SORT_FIELDS_ALL_PRODUCT_VARIATION_VIEW = List.of("price", "dateCreated");
    public static final List<String> ALLOWED_SORT_FIELDS_ALL_PRODUCT_VIEW_ADMIN = List.of("name", "dateCreated", "brand");
    public static final List<String> ALLOWED_ORDER_FIELDS = List.of("asc", "desc");
}
