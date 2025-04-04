package com.ecommerce.application;

import com.ecommerce.application.entity.Role;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.enums.RoleEnum;
import com.ecommerce.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
public class Application {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void adminCreation() {
		String adminEmail = "admin123@gmail.com";

		if (userRepository.findByEmail(adminEmail).isEmpty()) {
			User adminUser = new User();
			adminUser.setEmail(adminEmail);
			adminUser.setFirstName("Admin");
			adminUser.setLastName("User");
			adminUser.setPassword(passwordEncoder.encode("admin123"));
			adminUser.setActive(true);
			adminUser.setPasswordUpdateDate(LocalDateTime.now());
			adminUser.setRoles(new Role(RoleEnum.ADMIN));

			userRepository.save(adminUser);
			logger.info("Admin user created: {}", adminUser.getEmail());
		} else {
			logger.info("Admin user already exists.");
		}
	}
}
