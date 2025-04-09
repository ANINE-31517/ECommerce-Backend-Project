package com.ecommerce.application.constant;

import java.util.List;

public class CustomerConstant {

    public static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "fullName", "email", "createdAt");

    private CustomerConstant() {}
}
