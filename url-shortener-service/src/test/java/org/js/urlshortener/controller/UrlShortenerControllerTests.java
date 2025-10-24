package org.js.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.js.urlshortener.controller.model.PostUrlShortenRequest;
import org.js.urlshortener.controller.model.PostUrlShortenResponse;
import org.js.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UrlShortenerController.class)
public class UrlShortenerControllerTests {

    @MockitoBean
    private UrlShortenerService urlShortenerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void test_validUrl_201() throws Exception {
        final String validUrl = "google.com";
        final int validForDays = 7;
        final LocalDateTime expiresAt = LocalDateTime.now().plusDays(validForDays);

        PostUrlShortenRequest request = PostUrlShortenRequest.builder()
                .url(validUrl)
                .validForDays(validForDays)
                .build();

        PostUrlShortenResponse mockResponse = PostUrlShortenResponse.builder()
                .shortCode("xyz789")
                .originalUrl(validUrl.toLowerCase())
                .expiresAt(expiresAt)
                .build();

        when(urlShortenerService.shortenUrl(any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/shorten")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shortCode").value( "xyz789"))
                .andExpect(jsonPath("$.originalUrl").value(validUrl))
                .andExpect(jsonPath("$.expiresAt").value(expiresAt.toString()));
   }

    @Test
    public void test_invalidUrl_400() throws Exception {
        final String invalidUrl = "example/";
        final int validForDays = 7;

        PostUrlShortenRequest invalidRequest = PostUrlShortenRequest.builder()
                .url(invalidUrl)
                .validForDays(validForDays)
                .build();


        mockMvc.perform(post("/api/shorten")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void test_invalidValidForDays_tooLow_returns400() throws Exception {
        PostUrlShortenRequest request = PostUrlShortenRequest.builder()
                .url("https://example.com")
                .validForDays(0)  // Invalid: less than 1
                .build();

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void test_invalidValidForDays_tooHigh_returns400() throws Exception {
        PostUrlShortenRequest request = PostUrlShortenRequest.builder()
                .url("https://example.com")
                .validForDays(366)  // Invalid: exceeds 365
                .build();

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void test_blankUrl_returns400() throws Exception {
        PostUrlShortenRequest request = PostUrlShortenRequest.builder()
                .url("")  // Blank URL
                .validForDays(7)
                .build();

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
