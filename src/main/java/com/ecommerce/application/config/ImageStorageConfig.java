package com.ecommerce.application.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ImageStorageConfig {

    @Value("${app.image.base-path:${user.home}/Desktop/Project/images}")
    private String basePath;
}
