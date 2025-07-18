package com.ecommerce.application.controller;

import com.ecommerce.application.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final MessageSource messageSource;

    @PostMapping("/users/{userId}/upload")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    public ResponseEntity<String> uploadUserImage(@PathVariable UUID userId,
                                                  @RequestParam("file") MultipartFile file) throws IOException {
        imageService.uploadUserImage(userId, file);
        String message = messageSource.getMessage("image.upload.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Resource> getUserImage(@PathVariable UUID userId) throws IOException {
        return imageService.getUserImage(userId);
    }

    @GetMapping("/product-variation/{productVariationId}")
    public ResponseEntity<Resource> getProductVariationImage(@PathVariable UUID productVariationId) throws IOException {
        return imageService.getProductVariationImage(productVariationId);
    }

    @GetMapping("/product-variation/secondary/{name}")
    public ResponseEntity<Resource> getProductVariationSecondaryImage(@PathVariable String name) throws IOException {
        return imageService.getProductVariationSecondaryImage(name);
    }
}
