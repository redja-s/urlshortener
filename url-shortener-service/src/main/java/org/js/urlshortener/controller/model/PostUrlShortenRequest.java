package org.js.urlshortener.controller.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUrlShortenRequest {

    @NotBlank(message = "URL cannot be blank")
    @Pattern(regexp = VALID_URL_REGEX)
    private String url;

    @Min(value = 1, message = "Valid for days must be at least 1")
    @Max(value = 365, message = "Valid for days cannot exceed 365")
    private Integer validForDays;

    private static final String VALID_URL_REGEX = "^(https?://)?" +                                    // Optional protocol
            "[a-zA-Z0-9]([a-zA-Z0-9_-]*[a-zA-Z0-9])?" +        // First domain part (allows underscores)
            "(\\.[a-zA-Z0-9]([a-zA-Z0-9_-]*[a-zA-Z0-9])?)*" +  // Additional domain parts
            "\\.[a-zA-Z]{2,}" +                                 // TLD (at least 2 chars)
            "(:[0-9]{1,5})?" +                                  // Optional port
            "(/.*)?$";
}
