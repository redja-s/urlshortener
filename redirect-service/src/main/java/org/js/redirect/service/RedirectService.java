package org.js.redirect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.js.redirect.exception.model.UrlExpiredException;
import org.js.redirect.exception.model.UrlNotFoundException;
import org.js.redirect.persistence.entity.UrlEntity;
import org.js.redirect.repository.UrlRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedirectService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UrlRepository urlRepository;

    private static final String CACHE_PREFIX = "url:";
    private static final long DEFAULT_TTL_HOURS = 24;
    private static final long MIN_TTL_SECONDS = 300; // 5 minutes

    /**
     * Get long URL for a given short code.
     * Uses Redis cache with DB fallback for high performance.
     *
     * @param shortCode The short code to look up
     * @return The original long URL
     * @throws UrlNotFoundException if short code doesn't exist
     * @throws UrlExpiredException if URL has expired
     */
    public String getLongUrl(String shortCode) {
        // Step 1: Try cache first (hot path - 90%+ of requests)
        String cachedUrl = redisTemplate.opsForValue().get(CACHE_PREFIX + shortCode);

        if (cachedUrl != null) {
            log.debug("Cache HIT for: {}", shortCode);
            return cachedUrl;
        }

        log.debug("Cache MISS for: {}", shortCode);

        // Step 2: Cache miss - query database (cold path)
        UrlEntity entity = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("URL not found: {}", shortCode);
                    return new UrlNotFoundException();
                });

        // Step 3: Check expiration
        if (entity.getExpiresAt() != null &&
                entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Expired URL accessed: {}", shortCode);

            // Clean up expired URL asynchronously
            urlRepository.delete(entity);

            throw new UrlExpiredException();
        }

        // Step 4: Warm cache for next request
        String longUrl = entity.getLongUrl();
        cacheUrl(shortCode, longUrl, entity.getExpiresAt());

        return longUrl;
    }

    /**
     * Cache a URL with appropriate TTL.
     *
     * @param shortCode The short code
     * @param longUrl The long URL to cache
     * @param expiresAt When the URL expires (null if no expiration)
     */
    private void cacheUrl(String shortCode, String longUrl, LocalDateTime expiresAt) {
        long ttl = calculateTtl(expiresAt);

        redisTemplate.opsForValue().set(
                CACHE_PREFIX + shortCode,
                longUrl,
                ttl,
                TimeUnit.SECONDS
        );

        log.info("Cached URL: {} (TTL: {}s)", shortCode, ttl);
    }

    /**
     * Calculate appropriate cache TTL based on URL expiration.
     *
     * @param expiresAt When the URL expires (null if no expiration)
     * @return TTL in seconds
     */
    private long calculateTtl(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            // No expiration - use default TTL
            return TimeUnit.HOURS.toSeconds(DEFAULT_TTL_HOURS);
        }

        long secondsUntilExpiry = Duration
                .between(LocalDateTime.now(), expiresAt)
                .getSeconds();

        // Don't cache if expiring very soon
        if (secondsUntilExpiry < MIN_TTL_SECONDS) {
            return MIN_TTL_SECONDS;
        }

        // Cap at default TTL even if URL valid longer (for cache freshness)
        return Math.min(secondsUntilExpiry, TimeUnit.HOURS.toSeconds(DEFAULT_TTL_HOURS));
    }

    /**
     * Invalidate cache entry for a short code.
     * Useful when URL is deleted or updated.
     *
     * @param shortCode The short code to invalidate
     */
    public void invalidateCache(String shortCode) {
        Boolean deleted = redisTemplate.delete(CACHE_PREFIX + shortCode);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Cache invalidated for: {}", shortCode);
        }
    }
}
