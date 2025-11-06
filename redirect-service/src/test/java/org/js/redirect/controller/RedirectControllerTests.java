package org.js.redirect.controller;

import org.js.redirect.exception.model.UrlExpiredException;
import org.js.redirect.exception.model.UrlNotFoundException;
import org.js.redirect.service.RedirectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RedirectController.class)
public class RedirectControllerTests {

    @MockitoBean
    private RedirectService redirectService;

    @Autowired
    private MockMvc mockMvc;

    private final String validShortCode = "123abc";

    @Test
    public void test_redirect_validShortCodeReturns301() throws Exception {
        final String longUrl = "https://google.com";

        when(redirectService.getLongUrl(validShortCode))
                .thenReturn(longUrl);

        mockMvc.perform(get("/" + validShortCode))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", longUrl))
                .andExpect(header().exists("Cache-Control"));

        verify(redirectService).getLongUrl(validShortCode);
    }

    @Test
    void test_redirect_notFoundReturns404() throws Exception {
        when(redirectService.getLongUrl(validShortCode))
                .thenThrow(new UrlNotFoundException());

        mockMvc.perform(get("/" + validShortCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void redirect_expired_returns410() throws Exception {
        when(redirectService.getLongUrl(validShortCode))
                .thenThrow(new UrlExpiredException());

        mockMvc.perform(get("/" + validShortCode))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").exists());
    }
}
