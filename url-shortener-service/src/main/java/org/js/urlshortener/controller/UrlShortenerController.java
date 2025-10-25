package org.js.urlshortener.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.js.urlshortener.controller.model.PostUrlShortenRequest;
import org.js.urlshortener.controller.model.ShortenResponse;
import org.js.urlshortener.service.UrlShortenerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(
            @Valid @RequestBody final PostUrlShortenRequest requestBody
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(urlShortenerService.shortenUrl(requestBody));
    }

    @GetMapping("/shorten/{shortUrl}")
    public ResponseEntity<ShortenResponse> getShortUrlDetails(
            @PathVariable("shortUrl") final String shortUrl
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(urlShortenerService.getShortCodeDetails(shortUrl));
    }

    @DeleteMapping("/shorten/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(
            @PathVariable("shortCode") final String shortCode
    ) {
        urlShortenerService.deleteByShortCode(shortCode);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
