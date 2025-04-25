package com.ecommerce.application.service;

import com.ecommerce.application.repository.ActivationTokenRepository;
import com.ecommerce.application.repository.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final TokenRepository tokenRepository;
    private final ActivationTokenRepository activationTokenRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        tokenRepository.deleteByLastUpdatedBefore(now.minusHours(24));
        activationTokenRepository.deleteByExpiryDateBefore(now);

        log.info("All the expired token have been cleared out from the Token entity!");
        log.info("Expired activation tokens deleted at {}", now);
    }
}
