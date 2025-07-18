package com.ecommerce.application.service;

import com.ecommerce.application.CO.ProductAddCO;
import com.ecommerce.application.CO.ProductVariationAddCO;
import com.ecommerce.application.CO.UpdateProductCO;
import com.ecommerce.application.CO.UpdateProductVariationCO;
import com.ecommerce.application.VO.*;
import com.ecommerce.application.config.ImageStorageConfig;
import com.ecommerce.application.constant.AdminConstant;
import com.ecommerce.application.constant.CategoryConstant;
import com.ecommerce.application.constant.ImageConstant;
import com.ecommerce.application.entity.*;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.exception.InternalServerException;
import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.exception.UnauthorizedException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final ImageStorageConfig imageStorageConfig;

    private static final List<String> allowedSortFields = CategoryConstant.ALLOWED_SORT_FIELDS;
    private static final List<String> allowedOrderFields = CategoryConstant.ALLOWED_ORDER_FIELDS;

    @Transactional
    public void addProduct(ProductAddCO request) {
        User user = SecurityUtil.getCurrentUser();
        if (!(user instanceof Seller)) {
            log.warn("User with the emailId: {} is not a seller!", user.getEmail());
            throw new UnauthorizedException("Only sellers can add products!");
        }

        Optional<Seller> optionalSeller = sellerRepository.findById(user.getId());
        if (optionalSeller.isEmpty()) {
            log.error("Seller with the emailId: {} not found!", user.getEmail());
            throw new ResourceNotFoundException("Seller not found!");
        }

        Seller seller = optionalSeller.get();

        UUID fieldId;
        try {
            fieldId = UUID.fromString(request.getCategoryId());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid category ID format!");
        }

        Category category = categoryRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid category ID!"));

        if (categoryRepository.hasSubCategories(category.getId())) {
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
            throw new UnauthorizedException("Only sellers can add products!");
        }

        Product product = productRepository.findByIdAndSellerIdAndIsDeletedFalse(productId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product does not found or does not belong to the current seller!"));

        return convertToProductViewVO(product);
    }

    public ProductViewVO convertToProductViewVO(Product product) {
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
            throw new UnauthorizedException("Only sellers can delete products");
        }

        Product product = productRepository.findByIdAndSellerIdAndIsDeletedFalse(productId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found or does not belong to the current seller!"));

        product.setDeleted(true);
        productRepository.save(product);

        log.info("Product with id {} deleted by seller with email {}", productId, seller.getEmail());
    }

    @Transactional
    public void updateProduct(UpdateProductCO request) {
        User user = SecurityUtil.getCurrentUser();
        if (!(user instanceof Seller seller)) {
            log.warn("Logged in user by the emailId: {} is not a seller!", user.getEmail());
            throw new UnauthorizedException("Only sellers can delete products");
        }

        String id = request.getId().trim();
        UUID productId;
        try {
            productId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid UUID for product!");
        }

        Product product = productRepository.findByIdAndSellerIdAndIsDeletedFalse(productId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found or does not belong to the current seller!"));

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
            throw new UnauthorizedException("Only sellers can add product variations.");
        }

        String id = request.getProductId().trim();
        UUID productId;
        try {
            productId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid UUID for product!");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Id not found!"));

        if (!product.isActive() || product.isDeleted()) {
            throw new BadRequestException("Product is either not active or is deleted!");
        }

        Category category = product.getCategory();
        List<CategoryMetaDataFieldValue> allowedFieldValues = categoryMetaDataFieldValueRepository.findByCategory(category);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> metadataMap;
        try {
            metadataMap = objectMapper.readValue(request.getMetadata(), new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Metadata is not appropriate!");
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
            Map<String, String> existingMetadataMap = objectMapper.readValue(existingMetadataJson, new TypeReference<Map<String, String>>() {});
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
        variation.setMetadata(objectMapper.writeValueAsString(metadataMap));
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

    public ProductVariationViewVO viewProductVariation(UUID productVariationId) {
        User user = SecurityUtil.getCurrentUser();

        if (!(user instanceof Seller seller)) {
            log.warn("Logged in user with the emailId: {} is not a seller!", user.getEmail());
            throw new UnauthorizedException("Sellers are authorized to view product variation!");
        }

        ProductVariation productVariation = productVariationRepository.findById(productVariationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variation does not found!"));

        Product product = productVariation.getProduct();

        if (!product.isActive() || product.isDeleted()) {
            log.warn("Product with id: {} is either not active or is deleted!", product.getId());
            throw new BadRequestException("Product is either not active or is deleted!");
        }

        if (!product.getSeller().getId().equals(user.getId())) {
            log.warn("Product variation with id: {} is not created by the seller with id : {} !", productVariationId, seller.getId());
            throw new BadRequestException("Logged in seller is not the creator of the product variation!");
        }

        return convertToProductVariationViewVO(productVariation);
    }

    public ProductVariationViewVO convertToProductVariationViewVO(ProductVariation productVariation) {
        String primaryImageUrl = "Image not uploaded!";

        for (String ext : ImageConstant.ALLOWED_EXTENSIONS) {
            Path path = Paths.get(imageStorageConfig.getBasePath(), "product-variation", productVariation.getId().toString() + "." + ext);
            if (Files.exists(path)) {
                primaryImageUrl = "http://localhost:8080/api/images/product-variation/" + productVariation.getId();
                break;
            }
        }
        return ProductVariationViewVO.builder()
                .id(productVariation.getId())
                .price(productVariation.getPrice())
                .isActive(productVariation.isActive())
                .quantityAvailable(productVariation.getQuantityAvailable())
                .metadata(productVariation.getMetadata())
                .primaryImageName(primaryImageUrl)
                .productViewVO(convertToProductViewVO(productVariation.getProduct()))
                .build();
    }

    public Page<ProductVariationViewVO> viewAllProductVariation(UUID id, int offset, int max, String sort, String order, String query) {
        User user = SecurityUtil.getCurrentUser();

        if (!(user instanceof Seller seller)) {
            log.warn("LoggedIn user with the emailId: {} is not a seller!", user.getEmail());
            throw new UnauthorizedException("Sellers are authorized to view product variation!");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product did not found!"));

        if (!product.isActive() || product.isDeleted()) {
            log.warn("Product id: {} is either not active or is deleted!", product.getId());
            throw new BadRequestException("Product is either not active or is deleted!");
        }

        if (!product.getSeller().getId().equals(user.getId())) {
            log.warn("Product with id: {} is not created by the seller with id : {} !", id, seller.getId());
            throw new BadRequestException("Logged in seller is not the creator of the product variation!");
        }

        if (!CategoryConstant.ALLOWED_SORT_FIELDS_ALL_PRODUCT_VARIATION_VIEW.contains(sort)) {
            log.error("Invalid sort type passed, choose among (price, dateCreated)!");
            throw new BadRequestException("Only 'price' and 'dateCreated' are allowed in sort field.");
        }

        if (!allowedOrderFields.contains(order)) {
            log.error("Invalid order type is passed, choose either asc or desc!");
            throw new BadRequestException("Only 'asc' and 'desc' are allowed in order field.");
        }

        Sort sortOrder = order.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();

        Pageable pageable = PageRequest.of(offset, max, sortOrder);

        Page<ProductVariation> productVariations;
        if (query != null && !query.isBlank()) {
            productVariations = productVariationRepository.findAllByMetadataContainingIgnoreCaseAndProduct(query, product, pageable);
        } else {
            productVariations = productVariationRepository.findAllByProduct(pageable, product);
        }
        log.info("Total product variations fetched are: {}", productVariations.getTotalElements());

        return productVariations.map(this::convertToProductVariationViewVO);
    }

    @Transactional
    public void updateProductVariation(UpdateProductVariationCO request,
                                       MultipartFile primaryImage,
                                       List<MultipartFile> secondaryImages) throws IOException {
        User user = SecurityUtil.getCurrentUser();

        if (!(user instanceof Seller seller)) {
            log.warn("User logged in via emailId: {} is not a seller!", user.getEmail());
            throw new UnauthorizedException("Only sellers can delete products");
        }

        String id = request.getProductVariationId().trim();
        UUID productVariationId;
        try {
            productVariationId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid UUID for product!");
        }

        ProductVariation productVariation = productVariationRepository.findById(productVariationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variation not found!"));

        Optional<Product> optionalProduct = productRepository.findByIdAndSellerIdAndIsDeletedFalse(productVariation.getProduct().getId(), seller.getId());

        if (optionalProduct.isEmpty()) {
            log.warn("Product does not found or is deleted under the seller {}", seller.getEmail());
            throw new ResourceNotFoundException("Product is either deleted or does not belong to the logged in seller!");
        }

        Product product = optionalProduct.get();

        if (!product.isActive()) {
            log.warn("Product is in-active!");
            throw new BadRequestException("Product is not active!");
        }

        if (request.getQuantityAvailable() != null)
            productVariation.setQuantityAvailable(request.getQuantityAvailable());

        if (request.getPrice() != null)
            productVariation.setPrice(request.getPrice());

        if (request.getIsActive() != null)
            productVariation.setActive(request.getIsActive());

        if (request.getMetadata() != null && !request.getMetadata().isBlank()) {
            Category category = product.getCategory();
            List<CategoryMetaDataFieldValue> allowedFieldValues = categoryMetaDataFieldValueRepository.findByCategory(category);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> metadataMap;
            try {
                metadataMap = objectMapper.readValue(request.getMetadata(), new TypeReference<Map<String, String>>() {});
            } catch (JsonProcessingException e) {
                throw new InternalServerException("Metadata is not appropriate!");
            }

            Map<String, Set<String>> fieldAllowedMap = new HashMap<>();

            for (CategoryMetaDataFieldValue cmv : allowedFieldValues) {
                Set<String> fieldValues = Arrays.stream(cmv.getFieldValues().split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());

                fieldAllowedMap.put(
                        cmv.getCategoryMetaDataField().getName(),
                        fieldValues
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
                Map<String, String> existingMetadataMap = objectMapper.readValue(existingMetadataJson, new TypeReference<Map<String, String>>() {});
                Set<String> existingKeys = new HashSet<>(existingMetadataMap.keySet());

                if (!currentKeys.equals(existingKeys)) {
                    throw new BadRequestException("Metadata structure doesn't match existing variations of this product.");
                }
            }

            productVariation.setMetadata(objectMapper.writeValueAsString(metadataMap));
        }

        if (primaryImage != null && !primaryImage.isEmpty()) {
            imageService.deleteExistingPrimaryImage(productVariationId);
            imageService.downloadAndStoreImageFromUrl(primaryImage, productVariationId, 0);
            String extension = FilenameUtils.getExtension(primaryImage.getOriginalFilename());
            String primaryImageName = productVariationId + "." + extension;
            productVariation.setPrimaryImageName(primaryImageName);
        }

        if (!secondaryImages.isEmpty()) {
            int imageCount = productVariation.getImageCount();
            for (MultipartFile secondaryImage: secondaryImages) {
                imageService.downloadAndStoreImageFromUrl(secondaryImage, productVariationId, imageCount);
                imageCount +=1;
            }
            productVariation.setImageCount(imageCount);
        }

        productVariationRepository.save(productVariation);

        log.info("Product variation updated under the seller: {}", seller.getEmail());
    }

    public CustomerProductViewVO customerProductView(String id) {
        UUID productId;
        try {
            productId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid UUID for product!");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product does not found!"));

        if (product.isDeleted() || !product.isActive()) {
            log.warn("Product is either deleted or in-active!");
            throw new BadRequestException("Product is either deleted or deActive!");
        }

        if (product.getProductVariations() == null || product.getProductVariations().isEmpty()) {
            log.error("Product has no product variations!");
            throw new BadRequestException("Product has no product variations!");
        }

        return convertToCustomerProductViewVO(product);
    }

    private CustomerProductViewVO convertToCustomerProductViewVO(Product product) {

        return CustomerProductViewVO.builder()
                .productId(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .active(product.isActive())
                .category(CategoryViewSummaryVO.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build())
                .productVariations(convertToCustomerProductVariationViewVO(product))
                .build();
    }

    private List<CustomerProductVariationViewVO> convertToCustomerProductVariationViewVO(Product product) {
        List<CustomerProductVariationViewVO> customerProductVariationViewVOS = new ArrayList<>();
        List<ProductVariation> productVariations = productVariationRepository.findByProduct(product);

        for (ProductVariation productVariation : productVariations) {
            List<String> secondaryImagesList = new ArrayList<>();

            for (int i=1; i<productVariation.getImageCount(); i++) {
                secondaryImagesList.add("http://localhost:8080/api/images/product-variation/secondary/" + productVariation.getId() + "(" + i + ")");
            }
            customerProductVariationViewVOS.add(
                    CustomerProductVariationViewVO.builder()
                            .id(productVariation.getId())
                            .price(productVariation.getPrice())
                            .quantityAvailable(productVariation.getQuantityAvailable())
                            .metadata(productVariation.getMetadata())
                            .isActive(productVariation.isActive())
                            .primaryImage("http://localhost:8080/api/images/product-variation/" + productVariation.getId())
                            .secondaryImages(secondaryImagesList)
                            .build()
            );
        }
        return customerProductVariationViewVOS;
    }

    public Page<CustomerProductViewVO> allCustomerProductView(UUID id, int offset, int max, String sort, String order, String query) {
        if (!CategoryConstant.ALLOWED_SORT_FIELDS_ALL_PRODUCT_VIEW_ADMIN.contains(sort)) {
            log.error("Invalid sorting type is passed, choose among (name, brand, dateCreated)!");
            throw new BadRequestException("Only 'name', 'brand' and 'dateCreated' are allowed in sort field.");
        }

        if (!allowedOrderFields.contains(order)) {
            log.error("Invalid ordering type passed, choose either asc or desc!");
            throw new BadRequestException("Only 'asc' and 'desc' are allowed in order field.");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category Id does not exists!"));

        if (categoryRepository.hasSubCategories(id)) {
            log.warn("Category passed is not a leaf category!");
            throw new BadRequestException("Category passed is not a leaf category!");
        }

        Sort sortOrder = order.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();

        Pageable pageable = PageRequest.of(offset, max, sortOrder);

        Page<Product> products;
        if (query != null && !query.isBlank()) {
            products = productRepository.findAllByCategoryAndIsActiveTrueAndIsDeletedFalseAndNameContainingIgnoreCase(category, query, pageable);
        } else {
            products = productRepository.findAllByCategoryAndIsActiveTrueAndIsDeletedFalse(category, pageable);
        }
        log.info("Total products fetched under customer are: {}", products.getTotalElements());

        return products.map(this::convertToCustomerProductViewVO);
    }

    public Page<CustomerProductViewVO> similarCustomerProductView(UUID id, int offset, int max, String sort, String order, String query) {
        if (!CategoryConstant.ALLOWED_SORT_FIELDS_ALL_PRODUCT_VIEW_ADMIN.contains(sort)) {
            log.error("Invalid sort type is passed, choose among (name, brand, dateCreated)!");
            throw new BadRequestException("Only 'name', 'brand' and 'dateCreated' are allowed in sort field.");
        }

        if (!allowedOrderFields.contains(order)) {
            log.error("Invalid order type passed, choose either asc or desc!");
            throw new BadRequestException("Only 'asc' and 'desc' are allowed in order field.");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Id does not exists!"));

        Sort sortOrder = order.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();

        Pageable pageable = PageRequest.of(offset, max, sortOrder);

        Page<Product> products;
        if (query != null && !query.isBlank()) {
            products = productRepository.findAllByCategoryAndBrandAndIsActiveTrueAndIsDeletedFalseAndIdNotAndNameContainingIgnoreCase(product.getCategory(), product.getBrand(), id, query, pageable);
        } else {
            products = productRepository.findAllByCategoryAndBrandAndIsActiveTrueAndIsDeletedFalseAndIdNot(product.getCategory(), product.getBrand(), id, pageable);
        }
        log.info("Total similar products fetched under customer are: {}", products.getTotalElements());

        return products.map(this::convertToCustomerProductViewVO);
    }

    public AdminProductViewVO adminProductView(String id) {
        UUID productId;
        try {
            productId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid UUID for product!");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product does not found!"));

        return convertToAdminProductViewVO(product);
    }

    private AdminProductViewVO convertToAdminProductViewVO(Product product) {

        return AdminProductViewVO.builder()
                .productId(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .active(product.isActive())
                .category(CategoryViewSummaryVO.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build())
                .productVariations(convertToProductVariationImageVO(product))
                .build();
    }

    private List<ProductVariationImageVO> convertToProductVariationImageVO(Product product) {
        List<ProductVariationImageVO> productVariationImageVOS = new ArrayList<>();
        List<ProductVariation> productVariations = productVariationRepository.findByProduct(product);

        for (ProductVariation productVariation : productVariations) {
            productVariationImageVOS.add(
                    ProductVariationImageVO.builder()
                            .variationId(productVariation.getId())
                            .primaryImageUrl("http://localhost:8080/api/images/product-variation/" + productVariation.getId())
                            .build()
            );
        }
        return productVariationImageVOS;
    }

    public Page<AdminProductViewVO> adminAllProductView(int offset, int max, String sort, String order, String query) {
        if (!CategoryConstant.ALLOWED_SORT_FIELDS_ALL_PRODUCT_VIEW_ADMIN.contains(sort)) {
            log.error("Invalid sorting type passed, choose among (name, brand, dateCreated)!");
            throw new BadRequestException("Only 'name', 'brand' and 'dateCreated' are allowed in sort field.");
        }

        if (!allowedOrderFields.contains(order)) {
            log.error("Invalid ordering type is passed, choose either asc or desc!");
            throw new BadRequestException("Only 'asc' and 'desc' are allowed in order field.");
        }

        Sort sortOrder = order.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();

        Pageable pageable = PageRequest.of(offset, max, sortOrder);

        Page<Product> products;
        if (query != null && !query.isBlank()) {
            UUID uuid;
            try {
                uuid = UUID.fromString(query);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid UUID for product!");
            }
            products = productRepository.findAllByCategoryIdOrSellerId(uuid, uuid, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        log.info("Total products fetched are: {}", products.getTotalElements());

        return products.map(this::convertToAdminProductViewVO);
    }

    public void activateProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product does not found with the given id!"));

        if (product.isActive()) {
            log.warn("Product is already in activated mode!");
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

    public void deActivateProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product does not found with the given id!"));

        if (!product.isActive()) {
            log.warn("Product is already in deActivated mode!");
            throw new BadRequestException("Product is already not active or has been deActivated by the admin!");
        }

        product.setActive(false);
        productRepository.save(product);

        String sellerEmail = product.getSeller().getEmail();
        String subject = "Product deActivated!";
        String body = "<p><strong>The following product is deActivated:</strong></p>" +
                "<p>Name: " + product.getName() + "</p>" +
                "<p>Brand: " + product.getBrand() + "</p>" +
                "<p>Seller: " + sellerEmail + "</p>";

        emailService.sendEmail(sellerEmail, subject, body);
        log.info("Product with id: {} has been deActivated by the admin!", product.getId());
    }
}
