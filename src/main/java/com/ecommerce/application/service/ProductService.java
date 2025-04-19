package com.ecommerce.application.service;

import com.ecommerce.application.CO.ProductAddCO;
import com.ecommerce.application.VO.CategoryViewSummaryVO;
import com.ecommerce.application.VO.ProductViewVO;
import com.ecommerce.application.constant.AdminConstant;
import com.ecommerce.application.entity.*;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.repository.CategoryRepository;
import com.ecommerce.application.repository.ProductRepository;
import com.ecommerce.application.repository.SellerRepository;
import com.ecommerce.application.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final SellerRepository sellerRepository;

    public void addProduct(ProductAddCO request) {
        User user = SecurityUtil.getCurrentUser();
        if (!(user instanceof Seller)) {
            log.warn("User with the emailId: {} is not a seller!", user.getEmail());
            throw new BadRequestException("Only sellers can add products");
        }

        Optional<Seller> optionalSeller = sellerRepository.findById(user.getId());
        if (optionalSeller.isEmpty()) {
            log.error("Seller with the emailId: {} not found!", user.getEmail());
            throw new BadRequestException("Seller not found!");
        }

        Seller seller = optionalSeller.get();

        UUID fieldId;
        try {
            fieldId = UUID.fromString(request.getCategoryId());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid category ID format!");
        }

        Category category = categoryRepository.findById(fieldId)
                .orElseThrow(() -> new BadRequestException("Invalid category ID!"));

        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            log.error("Product can only be added to a leaf category!");
            throw new BadRequestException("Product can only be added to a leaf category!");
        }

        boolean exists = productRepository.existsByNameAndBrandAndCategoryAndSeller(
                request.getName(), request.getBrand(), category, seller);
        if (exists) {
            log.warn("Product can not be added under seller {} because of duplication!", seller.getEmail());
            throw new BadRequestException("Product with same name, brand, category and seller already exists!");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setCancellable(request.getCancellable());
        product.setReturnable(request.getReturnable());
        product.setSeller(seller);
        product.setActive(false);

        productRepository.save(product);

        String adminEmail = AdminConstant.ADMIN_EMAIL;
        String subject = "New Product Added: Awaiting Approval";
        String body = "<p><strong>New Product:</strong></p>" +
                "<p>Name: " + product.getName() + "</p>" +
                "<p>Brand: " + product.getBrand() + "</p>" +
                "<p>Seller: " + seller.getEmail() + "</p>";

        log.info("Product added successfully under seller {}", seller.getEmail());
        emailService.sendEmail(adminEmail, subject, body);
    }

    public ProductViewVO viewProduct(String id) {
        User user = SecurityUtil.getCurrentUser();
        if (!(user instanceof Seller seller)) {
            log.warn("User logged in with the emailId: {} is not a seller!", user.getEmail());
            throw new BadRequestException("Only sellers can add products");
        }

        UUID productId;
        try {
            productId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid product ID format!");
        }

        Product product = productRepository.findByIdAndSellerIdAndIsDeletedFalse(productId, seller.getId())
                .orElseThrow(() -> new BadRequestException("Product not found or does not belong to the current seller"));

        return convertToProductViewVO(product);
    }

    private ProductViewVO convertToProductViewVO(Product product) {
        return ProductViewVO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .active(product.isActive())
                .cancellable(product.isCancellable())
                .returnable(product.isReturnable())
                .category(CategoryViewSummaryVO.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build())
                .build();
    }
}
