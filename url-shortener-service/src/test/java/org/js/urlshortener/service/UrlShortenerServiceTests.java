package org.js.urlshortener.service;

import org.js.urlshortener.controller.mapper.UrlMapper;
import org.js.urlshortener.controller.model.PostUrlShortenRequest;
import org.js.urlshortener.controller.model.PostUrlShortenResponse;
import org.js.urlshortener.persistence.entity.UrlEntity;
import org.js.urlshortener.repository.UrlShortenerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UrlShortenerServiceTests {

    @Mock
    private UrlShortenerRepository urlShortenerRepository;

    @Mock
    private UrlMapper urlMapper;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    private PostUrlShortenRequest request;

    @BeforeEach
    void setUp() {
        request = PostUrlShortenRequest.builder().build();
    }

    @Test
    public void test_validUrl_noExpiration() {
        final String validUrl = "google.com/";
        request.setUrl(validUrl);

        when(urlShortenerRepository.findByShortCode(any()))
                .thenReturn(Optional.empty());
        when(urlShortenerRepository.save(any()))
                .thenReturn(UrlEntity.builder()
                        .id(100L)
                        .longUrl(validUrl)
                        .shortCode("abcdef")
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .build());

        PostUrlShortenResponse response = urlShortenerService.shortenUrl(request);

        assertEquals(validUrl, response.getOriginalUrl());
        assertEquals(LocalDateTime.now().plusDays(1), response.getExpiresAt());
        assertEquals(6, response.getShortCode().length());
    }


    @Test
    public void test_validUrl_noValidForDays_defaultsToOneDay() {
        // Given
        final String validUrl = "https://google.com";
        request.setUrl(validUrl);
        // Note: validForDays is null

        LocalDateTime mockCreatedAt = LocalDateTime.of(2023, 10, 1, 12, 0);
        LocalDateTime mockExpiresAt = mockCreatedAt.plusDays(1); // Should default to 1 day

        UrlEntity mockSavedEntity = UrlEntity.builder()
                .id(100L)
                .longUrl(validUrl.toLowerCase())
                .shortCode("abc123")
                .createdAt(mockCreatedAt)
                .expiresAt(mockExpiresAt)
                .build();

        PostUrlShortenResponse mockResponse = PostUrlShortenResponse.builder()
                .shortCode("abc123")
                .originalUrl(validUrl.toLowerCase())
                .expiresAt(mockExpiresAt)
                .build();

        // When
        when(urlShortenerRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(urlMapper.mapToUrlEntity(any(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockSavedEntity);
        when(urlShortenerRepository.save(any(UrlEntity.class))).thenReturn(mockSavedEntity);
        when(urlMapper.mapUrlEntityToResponse(mockSavedEntity)).thenReturn(mockResponse);

        // Then
        PostUrlShortenResponse response = urlShortenerService.shortenUrl(request);

        // Verify the default validForDays was set
        assertEquals(1, request.getValidForDays()); // This tests your business logic!

        // Verify the mapper was called with correct parameters
        verify(urlMapper).mapToUrlEntity(
                eq(request),
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );

        assertNotNull(response);
        assertEquals("abc123", response.getShortCode());
        assertEquals(validUrl.toLowerCase(), response.getOriginalUrl());
    }

    @Test
    public void test_validUrl_withCustomValidForDays_usesProvidedValue() {
        // Given
        final String validUrl = "https://example.com";
        final int customDays = 7;
        request.setUrl(validUrl);
        request.setValidForDays(customDays);

        // Setup mocks similar to above...
        when(urlShortenerRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(urlMapper.mapToUrlEntity(any(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(any(UrlEntity.class));
        when(urlShortenerRepository.save(any())).thenReturn(any(UrlEntity.class));
        when(urlMapper.mapUrlEntityToResponse(any())).thenReturn(any(PostUrlShortenResponse.class));

        // When
        urlShortenerService.shortenUrl(request);

        // Then
        assertEquals(customDays, request.getValidForDays()); // Should remain unchanged
    }

    @Test
    public void test_expiredShortCode_getsReused() {
        // Given
        final String validUrl = "https://test.com";
        request.setUrl(validUrl);

        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        UrlEntity expiredEntity = UrlEntity.builder()
                .shortCode("expired")
                .expiresAt(pastDate)
                .build();

        // When
        when(urlShortenerRepository.findByShortCode(anyString()))
                .thenReturn(Optional.of(expiredEntity)); // First call returns expired
        when(urlMapper.mapToUrlEntity(any(), anyString(), any(), any())).thenReturn(any(UrlEntity.class));
        when(urlShortenerRepository.save(any())).thenReturn(any(UrlEntity.class));
        when(urlMapper.mapUrlEntityToResponse(any())).thenReturn(any(PostUrlShortenResponse.class));

        // Then
        urlShortenerService.shortenUrl(request);

        verify(urlShortenerRepository).delete(expiredEntity); // Expired code should be deleted
    }
}
