package org.js.redirect.service;

import org.js.redirect.exception.model.UrlExpiredException;
import org.js.redirect.persistence.entity.UrlEntity;
import org.js.redirect.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RedirectServiceTests {
    @Mock
    private UrlRepository urlRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedirectService redirectService;

    private final String validShortCode = "xyz789";
    private final String cachePrefix = "url:";

    @Test
    public void test_getLongUrl_cacheHitNoDbQuery() {
        final String longUrl = "https://example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cachePrefix + validShortCode))
                .thenReturn(longUrl);

        final String getLongUrl = redirectService.getLongUrl(validShortCode);

        assertEquals(longUrl, getLongUrl);

        verify(urlRepository, never()).findByShortCode(any());
        verify(valueOperations, never()).set(any(), any(), anyLong(), any());
    }

    @Test
    public void test_getLongUrl_cacheMissDbQuery() {
        final String longUrl = "https://example.com";
        final LocalDateTime expiresAt = LocalDateTime.now().plusDays(6);
        UrlEntity urlEntity = UrlEntity.builder()
                .shortCode("xyz789")
                .longUrl(longUrl)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cachePrefix + validShortCode))
                .thenReturn(null);
        when(urlRepository.findByShortCode(validShortCode))
                .thenReturn(Optional.of(urlEntity));

        final String result = redirectService.getLongUrl(validShortCode);

        assertEquals(longUrl, result);
        verify(valueOperations).get(cachePrefix + validShortCode);
        verify(urlRepository).findByShortCode(validShortCode);
        verify(valueOperations).set(
                eq(cachePrefix + validShortCode),
                eq(longUrl),
                anyLong(),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    public void test_getLongUrl_whenUrlExpiredThrowException() {
        final String longUrl = "https://example.com";
        UrlEntity urlEntity = UrlEntity.builder()
                .shortCode("xyz789")
                .longUrl(longUrl)
                .createdAt(LocalDateTime.now().minusDays(3))
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cachePrefix + validShortCode))
                .thenReturn(null);
        when(urlRepository.findByShortCode(validShortCode))
                .thenReturn(Optional.of(urlEntity));

        assertThrows(UrlExpiredException.class,
                () -> redirectService.getLongUrl(validShortCode));

        verify(urlRepository, times(1)).findByShortCode(any());
        verify(valueOperations, never()).set(any(), any());
        verify(urlRepository).delete(urlEntity);
    }

    @Test
    public void test_invalidateCache_deletesFromRedis() {
        when(redisTemplate.delete(cachePrefix + validShortCode))
                .thenReturn(true);

        redirectService.invalidateCache(validShortCode);

        verify(redisTemplate).delete(cachePrefix + validShortCode);
    }
}
