package com.ecommerce.application.controller;

import com.ecommerce.application.service.ImageService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/users/{userId}/upload")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    public ResponseEntity<String> uploadUserImage(@PathVariable UUID userId,
                                                  @RequestParam("file") MultipartFile file) throws IOException {
        imageService.uploadUserImage(userId, file);
        return ResponseEntity.ok("User image uploaded successfully!");
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Resource> getUserImage(@PathVariable UUID userId) throws IOException {
        return imageService.getUserImage(userId);
    }
}
