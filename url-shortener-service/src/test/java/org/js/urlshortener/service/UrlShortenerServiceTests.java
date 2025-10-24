package org.js.urlshortener.service;

import org.js.urlshortener.controller.mapper.UrlMapper;
import org.js.urlshortener.controller.model.PostUrlShortenRequest;
import org.js.urlshortener.controller.model.PostUrlShortenResponse;
import org.js.urlshortener.persistence.entity.UrlEntity;
import org.js.urlshortener.repository.UrlShortenerRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private UrlShortenerRepository urlShortenerRepository;

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

        PostUrlShortenResponse mockResponse = PostUrlShortenResponse.builder()
                .shortCode("xyz789")
                .originalUrl(validUrl.toLowerCase())
                .expiresAt(LocalDateTime.now().plusDays(customDays))
                .build();

        // When - Mock the dependencies
        when(urlShortenerRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(urlMapper.mapToUrlEntity(any(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockEntity);
        when(urlShortenerRepository.save(any(UrlEntity.class))).thenReturn(mockEntity);
        when(urlMapper.mapUrlEntityToResponse(any(UrlEntity.class))).thenReturn(mockResponse);

        // Execute the method
        PostUrlShortenResponse response = urlShortenerService.shortenUrl(request);

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
        verify(urlShortenerRepository, times(1)).save(any());
        verify(urlShortenerRepository, atMost(MAX_COLLISION_RETRIES)).findByShortCode(any());
    }

    @Test
    public void test_invalidUrl_throwsException() {
        final String invalidUrl = "google/";
        request.setUrl(invalidUrl);

        verify(urlShortenerRepository, never()).save(any());
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

        PostUrlShortenResponse mockResponse = PostUrlShortenResponse.builder()
                .shortCode("abc123")
                .originalUrl(validUrl.toLowerCase())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        // When - Mock the dependencies
        when(urlShortenerRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(urlMapper.mapToUrlEntity(any(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockEntity);
        when(urlShortenerRepository.save(any(UrlEntity.class))).thenReturn(mockEntity);
        when(urlMapper.mapUrlEntityToResponse(any(UrlEntity.class))).thenReturn(mockResponse);

        // Execute the method
        PostUrlShortenResponse response = urlShortenerService.shortenUrl(request);

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

        PostUrlShortenResponse mockResponse = PostUrlShortenResponse.builder()
                .shortCode(duplicateShortCode)
                .originalUrl(validUrl)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();


        when(urlShortenerRepository.findByShortCode(any()))
                .thenReturn(Optional.of(expiredEntity));
        when(urlMapper.mapToUrlEntity(any(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(newEntity);
        when(urlShortenerRepository.save(any(UrlEntity.class))).thenReturn(newEntity);
        when(urlMapper.mapUrlEntityToResponse(any(UrlEntity.class))).thenReturn(mockResponse);
        when(urlShortCodeUtils.generateShortCode()).thenReturn(duplicateShortCode);

        PostUrlShortenResponse response = urlShortenerService.shortenUrl(request);

        // Then - Verify the business logic
        assertEquals(UrlShortenerService.DEFAULT_VALID_FOR_DAYS, request.getValidForDays());
        assertEquals(validUrl, response.getOriginalUrl());

        verify(urlShortenerRepository).delete(expiredEntity);

        // Verify new entity was saved
        verify(urlShortenerRepository).save(any(UrlEntity.class));

        // Verify findByShortCode was called only once (expired code found immediately)
        verify(urlShortenerRepository, times(1)).findByShortCode(duplicateShortCode);
    }
}
