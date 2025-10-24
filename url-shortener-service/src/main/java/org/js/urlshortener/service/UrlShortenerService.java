package org.js.urlshortener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.js.urlshortener.controller.mapper.UrlMapper;
import org.js.urlshortener.controller.model.PostUrlShortenRequest;
import org.js.urlshortener.controller.model.PostUrlShortenResponse;
import org.js.urlshortener.persistence.entity.UrlEntity;
import org.js.urlshortener.repository.UrlShortenerRepository;
import org.js.urlshortener.utils.UrlShortCodeUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerService {

    public static final int DEFAULT_VALID_FOR_DAYS = 1;
    public static final int MAX_COLLISION_RETRIES = 10;

    private final UrlShortenerRepository urlShortenerRepository;
    private final UrlMapper urlMapper;
    private final UrlShortCodeUtils urlShortCodeUtils;

    public PostUrlShortenResponse shortenUrl(final PostUrlShortenRequest urlShortenRequest) {
        final String urlToShorten = urlShortenRequest.getUrl().toLowerCase();

        if (urlShortenRequest.getValidForDays() == null) {
            urlShortenRequest.setValidForDays(DEFAULT_VALID_FOR_DAYS);
        }

        // Generate unique short code
        String shortCode = generateUniqueShortCode();

        // Calculate expiration date
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(urlShortenRequest.getValidForDays());
        LocalDateTime createdAt = LocalDateTime.now();

        // Create and save URL entity using MapStruct
        UrlEntity urlEntity = urlMapper.mapToUrlEntity(urlShortenRequest, shortCode, createdAt, expiresAt);

        UrlEntity savedEntity = urlShortenerRepository.save(urlEntity);
        log.info("Created short URL: {} -> {}", shortCode, urlToShorten);

        // Return response
        return urlMapper.mapUrlEntityToResponse(savedEntity);
    }

    private String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;

        do {
            shortCode = urlShortCodeUtils.generateShortCode();
            attempts++;

            if (attempts > MAX_COLLISION_RETRIES) {
                log.error("Failed to generate unique short code after {} attempts", MAX_COLLISION_RETRIES);
                throw new RuntimeException("Unable to generate unique short code");
            }

            // Check if code exists and is still valid
            Optional<UrlEntity> existingUrl = urlShortenerRepository.findByShortCode(shortCode);

            if (existingUrl.isEmpty()) {
                // Code doesn't exist, we can use it
                break;
            }

            if (existingUrl.get().getExpiresAt().isBefore(LocalDateTime.now())) {
                // Code exists but is expired, delete it and reuse
                urlShortenerRepository.delete(existingUrl.get());
                log.info("Reusing expired short code: {}", shortCode);
                break;
            }

            // Code exists and is still valid, try again

        } while (true);

        return shortCode;
    }
}
