package com.ecommerce.application;

import com.ecommerce.application.constant.AdminConstant;
import com.ecommerce.application.entity.Role;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.enums.RoleEnum;
import com.ecommerce.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
@RequiredArgsConstructor
@EnableAsync
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Slf4j
public class Application {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void adminCreation() {
		String adminEmail = AdminConstant.ADMIN_EMAIL;
		String password = AdminConstant.ADMIN_PASSWORD;

		if (userRepository.findByEmail(adminEmail).isEmpty()) {
			User adminUser = new User();
			adminUser.setEmail(adminEmail);
			adminUser.setFirstName("Admin");
			adminUser.setLastName("User");
			adminUser.setPassword(passwordEncoder.encode(password));
			adminUser.setActive(true);
			adminUser.setPasswordUpdateDate(LocalDateTime.now());
			adminUser.setRoles(new Role(RoleEnum.ADMIN));
			adminUser.setCreatedAt(LocalDateTime.now());

			userRepository.save(adminUser);
			log.info("\n Registration Successful! \n User: {} \n Role: {}",
					adminUser.getEmail(),
					adminUser.getRoles().getAuthority());
		} else {
			logger.info("Admin user already exists.");
		}
	}
}
