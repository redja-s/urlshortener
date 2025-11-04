package org.js.urlshortener.service;

import org.js.urlshortener.controller.mapper.UrlMapper;
import org.js.urlshortener.controller.model.PostUrlShortenRequest;
import org.js.urlshortener.controller.model.ShortenResponse;
import org.js.urlshortener.exception.model.UrlNotFoundException;
import org.js.urlshortener.persistence.entity.UrlEntity;
import org.js.urlshortener.repository.UrlRepository;
import org.js.urlshortener.utils.UrlShortCodeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.js.urlshortener.service.UrlShortenerService.MAX_COLLISION_RETRIES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UrlShortenerServiceTests {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlMapper urlMapper;

    @Mock
    private UrlShortCodeUtils urlShortCodeUtils;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    private PostUrlShortenRequest request;

    @BeforeEach
    void setUp() {
        request = PostUrlShortenRequest.builder().build();
    }

    @Test
    public void test_validRequest_happyPath() {
        // Given
        final String validUrl = "https://example.com";
        final int customDays = 7;
        request.setUrl(validUrl);
        request.setValidForDays(customDays);

        UrlEntity mockEntity = UrlEntity.builder()
                .shortCode("xyz789")
                .longUrl(validUrl.toLowerCase())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(customDays))
                .build();

        ShortenResponse mockResponse = ShortenResponse.builder()
                .shortCode("xyz789")
                .originalUrl(validUrl.toLowerCase())
                .expiresAt(LocalDateTime.now().plusDays(customDays))
                .build();

        // When - Mock the dependencies
        when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(urlMapper.mapToUrlEntity(any(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockEntity);
        when(urlRepository.save(any(UrlEntity.class))).thenReturn(mockEntity);
        when(urlMapper.mapUrlEntityToResponse(any(UrlEntity.class))).thenReturn(mockResponse);

        // Execute the method
        ShortenResponse response = urlShortenerService.shortenUrl(request);

        // Then - Verify the business logic
        assertEquals(customDays, request.getValidForDays(),
                "validForDays should remain unchanged when provided");

        // Verify that the mapper was called with the original custom value
        verify(urlMapper).mapToUrlEntity(
                argThat(req -> req.getValidForDays().equals(customDays)),
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );

        assertNotNull(response);
        // Verifies .save called only once
        verify(urlRepository, times(1)).save(any());
        verify(urlRepository, atMost(MAX_COLLISION_RETRIES)).findByShortCode(any());
    }

    @Test
    public void test_invalidUrl_throwsException() {
        final String invalidUrl = "google/";
        request.setUrl(invalidUrl);

        verify(urlRepository, never()).save(any());
        verify(urlMapper, never()).mapToUrlEntity(any(), any(), any(), any());
        verify(urlMapper, never()).mapUrlEntityToResponse(any());
    }

    @Test
    public void test_requestWithoutValidForDays_setsDefaultValue() {
        // Given
        final String validUrl = "https://google.com";
        request.setUrl(validUrl);
        request.setValidForDays(null); // Explicitly set to null

        UrlEntity mockEntity = UrlEntity.builder()
                .shortCode("abc123")
                .longUrl(validUrl.toLowerCase())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        ShortenResponse mockResponse = ShortenResponse.builder()
                .shortCode("abc123")
                .originalUrl(validUrl.toLowerCase())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        // When - Mock the dependencies
        when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(urlMapper.mapToUrlEntity(any(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockEntity);
        when(urlRepository.save(any(UrlEntity.class))).thenReturn(mockEntity);
        when(urlMapper.mapUrlEntityToResponse(any(UrlEntity.class))).thenReturn(mockResponse);

        // Execute the method
        ShortenResponse response = urlShortenerService.shortenUrl(request);

        // Then - Verify the business logic
        assertEquals(UrlShortenerService.DEFAULT_VALID_FOR_DAYS, request.getValidForDays(),
                "validForDays should be set to default value when null");

        // Verify that the mapper was called with the request that now has the default value
        verify(urlMapper).mapToUrlEntity(
                argThat(req -> req.getValidForDays().equals(UrlShortenerService.DEFAULT_VALID_FOR_DAYS)),
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );

        assertNotNull(response);
    }

    @Test
    public void test_generateExpiredShortCode() {
        // Given
        final String validUrl = "https://google.com";
        request.setUrl(validUrl);

        final String duplicateShortCode = "abc123";

        UrlEntity expiredEntity = UrlEntity.builder()
                .shortCode(duplicateShortCode)
                .longUrl("randomUrl.com")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().minusDays(10))
                .build();

        UrlEntity newEntity = UrlEntity.builder()
                .shortCode(duplicateShortCode)
                .longUrl(validUrl)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        ShortenResponse mockResponse = ShortenResponse.builder()
                .shortCode(duplicateShortCode)
                .originalUrl(validUrl)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();


        when(urlRepository.findByShortCode(any()))
                .thenReturn(Optional.of(expiredEntity));
        when(urlMapper.mapToUrlEntity(any(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(newEntity);
        when(urlRepository.save(any(UrlEntity.class))).thenReturn(newEntity);
        when(urlMapper.mapUrlEntityToResponse(any(UrlEntity.class))).thenReturn(mockResponse);
        when(urlShortCodeUtils.generateShortCode()).thenReturn(duplicateShortCode);

        ShortenResponse response = urlShortenerService.shortenUrl(request);

        // Then - Verify the business logic
        assertEquals(UrlShortenerService.DEFAULT_VALID_FOR_DAYS, request.getValidForDays());
        assertEquals(validUrl, response.getOriginalUrl());

        verify(urlRepository).delete(expiredEntity);

        // Verify new entity was saved
        verify(urlRepository).save(any(UrlEntity.class));

        // Verify findByShortCode was called only once (expired code found immediately)
        verify(urlRepository, times(1)).findByShortCode(duplicateShortCode);
    }

    @Test
    public void test_getShortCode_happyPath() {
        final String validShortCode = "123abc";
        final String longUrl = "google.com";
        final LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        final LocalDateTime expiresAt = createdAt.plusDays(10);

        final UrlEntity urlEntity = UrlEntity.builder()
                .id(100L)
                .expiresAt(expiresAt)
                .createdAt(createdAt)
                .shortCode(validShortCode)
                .longUrl(longUrl)
                .build();

        final ShortenResponse shortenResponse = ShortenResponse.builder()
                .shortCode(validShortCode)
                .originalUrl(longUrl)
                .expiresAt(expiresAt)
                .createdAt(createdAt)
                .build();

        when(urlRepository.findByShortCode(validShortCode))
                .thenReturn(Optional.of(urlEntity));
        when(urlMapper.mapUrlEntityToResponse(any()))
                .thenReturn(shortenResponse);

        final ShortenResponse getByShortCode = urlShortenerService.getShortCodeDetails(validShortCode);

        assertEquals(validShortCode, getByShortCode.getShortCode());
    }

    @Test
    public void test_getShortCode_notFoundExceptionThrown() {
        final String validShortCode = "123abc";

        when(urlRepository.findByShortCode(validShortCode))
                .thenThrow(new UrlNotFoundException());

        assertThrows(UrlNotFoundException.class,
                () -> urlShortenerService.getShortCodeDetails(validShortCode));

        verify(urlMapper, times(0)).mapUrlEntityToResponse(any());
    }

    @Test
    public void test_deleteByShortCode_success() {
        // Given
        final String shortCode = "abc123";

        UrlEntity existingEntity = UrlEntity.builder()
                .shortCode(shortCode)
                .longUrl("https://example.com")
                .build();

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(existingEntity));

        // When
        urlShortenerService.deleteByShortCode(shortCode);

        // Then
        verify(urlRepository).findByShortCode(shortCode);
        verify(urlRepository).deleteByShortCode(shortCode);
    }

    @Test
    public void test_deleteByShortCode_notFound_throwsException() {
        // Given
        final String shortCode = "nonexistent";

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UrlNotFoundException.class, () ->
                urlShortenerService.deleteByShortCode(shortCode)
        );

        verify(urlRepository).findByShortCode(shortCode);
        verify(urlRepository, never()).deleteByShortCode(anyString());
    }
}
