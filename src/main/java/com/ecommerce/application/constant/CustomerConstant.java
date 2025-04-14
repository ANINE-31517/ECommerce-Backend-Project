package com.ecommerce.application.constant;

import java.util.List;

public class CustomerConstant {

    private CustomerConstant() {}

    public static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "email", "createdAt");
}
