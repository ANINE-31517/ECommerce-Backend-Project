package com.ecommerce.application.service;

import com.ecommerce.application.CO.ProductAddCO;
import com.ecommerce.application.CO.ProductVariationAddCO;
import com.ecommerce.application.CO.UpdateProductCO;
import com.ecommerce.application.VO.CategoryViewSummaryVO;
import com.ecommerce.application.VO.ProductViewVO;
import com.ecommerce.application.constant.AdminConstant;
import com.ecommerce.application.constant.CategoryConstant;
import com.ecommerce.application.entity.*;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.repository.*;
import com.ecommerce.application.security.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final SellerRepository sellerRepository;
    private final ProductVariationRepository productVariationRepository;
    private final CategoryMetaDataFieldValueRepository categoryMetaDataFieldValueRepository;
    private final ImageService imageService;

    private static final List<String> allowedSortFields = CategoryConstant.ALLOWED_SORT_FIELDS;
    private static final List<String> allowedOrderFields = CategoryConstant.ALLOWED_ORDER_FIELDS;

    @Transactional
    public void addProduct(ProductAddCO request) {
        User user = SecurityUtil.getCurrentUser();
        if (!(user instanceof Seller)) {
            log.warn("User with the emailId: {} is not a seller!", user.getEmail());
            throw new BadRequestException("Only sellers can add products!");
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

    public ProductViewVO viewProduct(UUID productId) {
        User user = SecurityUtil.getCurrentUser();
        if (!(user instanceof Seller seller)) {
            log.warn("User logged in with the emailId: {} is not a seller!", user.getEmail());
            throw new BadRequestException("Only sellers can add products!");
        }

        Product product = productRepository.findByIdAndSellerIdAndIsDeletedFalse(productId, seller.getId())
                .orElseThrow(() -> new BadRequestException("Product does not found or does not belong to the current seller!"));

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

    public Page<ProductViewVO> viewAllProduct(int offset, int max, String sort, String order, String query) {
        if (!allowedSortFields.contains(sort)) {
            log.error("Invalid sort type passed, choose among (name, dateCreated)");
            throw new BadRequestException("Only 'name' and 'dateCreated' are allowed in sort field.");
        }

        if (!allowedOrderFields.contains(order)) {
            log.error("Invalid order type is passed, choose among (asc or desc)");
            throw new BadRequestException("Only 'asc' and 'desc' are allowed in order field.");
        }

        Sort sortOrder = order.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();

        Pageable pageable = PageRequest.of(offset, max, sortOrder);

        Page<Product> products;
        if (query != null && !query.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCase(query, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        log.info("Total categories fetched: {}", products.getTotalElements());

        return products.map(this::convertToProductViewVO);
    }

    public void deleteProduct(UUID productId) {
        User user = SecurityUtil.getCurrentUser();
        if (!(user instanceof Seller seller)) {
            log.warn("User logged in by the emailId: {} is not a seller!", user.getEmail());
            throw new BadRequestException("Only sellers can delete products");
        }

        Product product = productRepository.findByIdAndSellerIdAndIsDeletedFalse(productId, seller.getId())
                .orElseThrow(() -> new BadRequestException("Product not found or does not belong to the current seller!"));

        product.setDeleted(true);
        productRepository.save(product);

        log.info("Product with id {} deleted by seller with email {}", productId, seller.getEmail());
    }

    @Transactional
    public void updateProduct(UpdateProductCO request) {
        User user = SecurityUtil.getCurrentUser();
        if (!(user instanceof Seller seller)) {
            log.warn("Logged in user by the emailId: {} is not a seller!", user.getEmail());
            throw new BadRequestException("Only sellers can delete products");
        }

        String id = request.getId().trim();
        UUID productId;
        try {
            productId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid UUID for product!");
        }

        Product product = productRepository.findByIdAndSellerIdAndIsDeletedFalse(productId, seller.getId())
                .orElseThrow(() -> new BadRequestException("Product not found or does not belong to the current seller!"));

        if (request.getName() != null && !request.getName().isBlank() && !request.getName().equals(product.getName())) {
            boolean exists = productRepository.existsByNameAndBrandAndCategoryAndSeller(
                    request.getName(), product.getBrand(), product.getCategory(), product.getSeller());

            if (exists) {
                throw new BadRequestException("Updated product name is not unique under the seller!");
            }

            product.setName(request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank())
            product.setDescription(request.getDescription());

        if (request.getCancellable() != null)
            product.setCancellable(request.getCancellable());

        if (request.getReturnable() != null)
            product.setReturnable(request.getReturnable());

        productRepository.save(product);

        log.info("Product Updated under the seller: {}", seller.getEmail());
    }

    @Transactional
    public void addProductVariation(ProductVariationAddCO request,
                                    MultipartFile primaryImage,
                                    List<MultipartFile> secondaryImages) throws IOException {
        User user = SecurityUtil.getCurrentUser();
        if (!(user instanceof Seller)) {
            log.warn("Unauthorized user attempting product variation add: {}", user.getEmail());
            throw new BadRequestException("Only sellers can add product variations.");
        }

        String id = request.getProductId().trim();
        UUID productId;
        try {
            productId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid UUID for product!");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product Id not found!"));

        if (!product.isActive() || product.isDeleted()) {
            throw new BadRequestException("Product is either not active or is deleted!");
        }

        Category category = product.getCategory();
        List<CategoryMetaDataFieldValue> allowedFieldValues = categoryMetaDataFieldValueRepository.findByCategory(category);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> metadataMap;
        try {
            metadataMap = objectMapper.readValue(request.getMetadata(), Map.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Metadata is not appropriate!");
        }

        Map<String, Set<String>> fieldAllowedMap = new HashMap<>();

        for (CategoryMetaDataFieldValue cmv : allowedFieldValues) {
            Set<String> filedValues = Arrays.stream(cmv.getFieldValues().split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            fieldAllowedMap.put(
                    cmv.getCategoryMetaDataField().getName(),
                    filedValues
            );
        }

        for (Map.Entry<String, String> entry : metadataMap.entrySet()) {
            String field = entry.getKey().toLowerCase();
            String value = entry.getValue().trim();
            Set<String> allowedValues = fieldAllowedMap.get(field);

            if (allowedValues == null || !allowedValues.contains(value)) {
                throw new BadRequestException("Invalid value for metadata field: " + field + "!");
            }
        }

        List<ProductVariation> existingVariations = productVariationRepository.findByProduct(product);

        if (!existingVariations.isEmpty()) {
            Set<String> currentKeys = new HashSet<>(metadataMap.keySet());

            String existingMetadataJson = existingVariations.getFirst().getMetadata();
            Map<String, String> existingMetadataMap = new ObjectMapper().readValue(existingMetadataJson, new TypeReference<>() {});
            Set<String> existingKeys = new HashSet<>(existingMetadataMap.keySet());

            if (!currentKeys.equals(existingKeys)) {
                throw new BadRequestException("Metadata structure doesn't match existing variations of this product.");
            }
        }

        ProductVariation variation = new ProductVariation();
        variation.setProduct(product);
        variation.setQuantityAvailable(request.getQuantityAvailable());
        variation.setPrice(request.getPrice());
        variation.setActive(true);
        variation.setDeleted(false);
        variation.setMetadata(new ObjectMapper().writeValueAsString(request.getMetadata()));

        productVariationRepository.save(variation);

        UUID variationId = variation.getId();
        int imageCount = 0;
        if (primaryImage == null || primaryImage.isEmpty()) {
            throw new BadRequestException("Primary Image is required!");
        }
        imageService.downloadAndStoreImageFromUrl(primaryImage, variationId, imageCount);
        imageCount += 1;

        if (!secondaryImages.isEmpty()) {
            for (MultipartFile secondaryImage: secondaryImages) {
                imageService.downloadAndStoreImageFromUrl(secondaryImage, variationId, imageCount);
                imageCount +=1;
            }
        }

        String extension = FilenameUtils.getExtension(primaryImage.getOriginalFilename());
        String primaryImageName = variation.getId() + "." + extension;
        variation.setPrimaryImageName(primaryImageName);

        variation.setImageCount(imageCount);
        productVariationRepository.save(variation);

        log.info("Product Variation added successfully with id: {}", variationId);
    }

    public void activateProduct(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Product does not found with the given id!"));

        if (product.isActive()) {
            throw new BadRequestException("Product is already activated by the admin!");
        }

        product.setActive(true);
        productRepository.save(product);

        String sellerEmail = product.getSeller().getEmail();
        String subject = "Product Activated!";
        String body = "<p><strong>The following product is activated:</strong></p>" +
                "<p>Name: " + product.getName() + "</p>" +
                "<p>Brand: " + product.getBrand() + "</p>" +
                "<p>Seller: " + sellerEmail + "</p>";

        emailService.sendEmail(sellerEmail, subject, body);
        log.info("Product with id: {} has been activated by the admin!", product.getId());
    }
}
