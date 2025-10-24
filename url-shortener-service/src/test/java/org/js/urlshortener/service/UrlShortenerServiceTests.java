package org.js.urlshortener.service;

import org.js.urlshortener.controller.mapper.UrlMapper;
import org.js.urlshortener.controller.model.PostUrlShortenRequest;
import org.js.urlshortener.controller.model.PostUrlShortenResponse;
import org.js.urlshortener.exception.model.InvalidUrlException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    public void test_invalidUrl_throwsException() {
        final String invalidUrl = "google/";
        request.setUrl(invalidUrl);

        assertThrows(InvalidUrlException.class,
                () -> urlShortenerService.shortenUrl(request));

        verify(urlShortenerRepository, never()).save(any());
        verify(urlMapper, never()).mapToUrlEntity(any(), any(), any(), any());
        verify(urlMapper, never()).mapUrlEntityToResponse(any());
    }
}
