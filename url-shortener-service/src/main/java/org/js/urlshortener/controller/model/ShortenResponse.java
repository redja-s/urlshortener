package org.js.urlshortener.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortenResponse {
    private String shortCode;
    private String originalUrl;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
