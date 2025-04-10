package com.ecommerce.application.security;

import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private SecurityUtil() {}

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("No authenticated user found!");
        }
        return (User) authentication.getPrincipal();
    }
}
