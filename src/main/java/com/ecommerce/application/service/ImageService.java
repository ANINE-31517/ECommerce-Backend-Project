package com.ecommerce.application.service;

import com.ecommerce.application.config.ImageStorageConfig;
import com.ecommerce.application.constant.ImageConstant;
import com.ecommerce.application.entity.ProductVariation;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.net.URL;


@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageStorageConfig imageStorageConfig;
    private final UserRepository userRepository;

    private static final List<String> allowedExtensions = ImageConstant.ALLOWED_EXTENSIONS;

    public void uploadUserImage(UUID userId, MultipartFile file) throws IOException {

        Optional<User> userOpt = userRepository.findById(userId);

        if(userOpt.isEmpty()) {
            throw new BadRequestException("User Id not found!");
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        if (!allowedExtensions.contains(extension)) {
            throw new BadRequestException("Only jpg, jpeg, png, bmp files are allowed!");
        }

        String filePath = getUserImagePath(userId, extension);

        File destFile = new File(filePath);
        destFile.getParentFile().mkdirs();
        file.transferTo(destFile);

        log.info("Image successfully uploaded for userId: {}", userId);
    }

    private String getUserImagePath(UUID userId, String extension) {
        return Paths.get(imageStorageConfig.getBasePath(), "users", userId.toString() + "." + extension).toString();
    }

    public ResponseEntity<Resource> getUserImage(UUID userId) throws IOException {

        Optional<User> userOpt = userRepository.findById(userId);

        if(userOpt.isEmpty()) {
            log.error("User Id not found!");
            throw new BadRequestException("User Id not found!");
        }

        Path directoryPath = Paths.get(imageStorageConfig.getBasePath(), "users");

        for (String ext : allowedExtensions) {
            Path path = directoryPath.resolve(userId.toString() + "." + ext);
            if (Files.exists(path)) {
                Resource resource = new UrlResource(path.toUri());
                MediaType mediaType = getMediaType(ext);
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .body(resource);
            }
        }
        throw new FileNotFoundException("User image not found!");
    }

    private MediaType getMediaType(String extension) {
        return switch (extension) {
            case "png" -> MediaType.IMAGE_PNG;
            case "bmp" -> MediaType.valueOf("image/bmp");
            default -> MediaType.IMAGE_JPEG;
        };
    }

    public void downloadAndStoreImageFromUrl(MultipartFile image, UUID productVariationId, int imageCount) throws IOException {
        String extension = FilenameUtils.getExtension(image.getOriginalFilename());

        if (!allowedExtensions.contains(extension)) {
            throw new BadRequestException("Only jpg, jpeg, png, bmp files are allowed!");
        }

        String filePath = getProductVariationImagePath(productVariationId, extension, imageCount);

        File destFile = new File(filePath);
        destFile.getParentFile().mkdirs();
        image.transferTo(destFile);

        log.info("Image successfully saved at: {}", filePath);
    }

    private String getProductVariationImagePath(UUID productVariationId, String extension, int imageCount) {
        if (imageCount == 0)
            return Paths.get(imageStorageConfig.getBasePath(), "product-variation", productVariationId.toString() + "." + extension).toString();
        return Paths.get(imageStorageConfig.getBasePath(), "product-variation", productVariationId.toString() +"(" + imageCount + ")" + "." + extension).toString();
    }
}

