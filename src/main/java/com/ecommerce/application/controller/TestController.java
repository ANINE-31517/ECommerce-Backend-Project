package com.ecommerce.application.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/admin")
    public String adminAccess() {
        return "Welcome, Admin!";
    }

    @GetMapping("/customer")
    public String userAccess() {
        return "Welcome, Customer!";
    }

    @GetMapping("/seller")
    public String sellerAccess() {
        return "Welcome, Seller!";
    }

    @GetMapping("/public")
    public String publicAccess() {
        return "Welcome, Public!";
    }

}