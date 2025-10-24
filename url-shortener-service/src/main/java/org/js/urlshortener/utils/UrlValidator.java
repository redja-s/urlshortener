package org.js.urlshortener.utils;

public class UrlValidator {
    private static final String VALID_URL_REGEX = "^(https?://)?" +                                    // Optional protocol
            "[a-zA-Z0-9]([a-zA-Z0-9_-]*[a-zA-Z0-9])?" +        // First domain part (allows underscores)
            "(\\.[a-zA-Z0-9]([a-zA-Z0-9_-]*[a-zA-Z0-9])?)*" +  // Additional domain parts
            "\\.[a-zA-Z]{2,}" +                                 // TLD (at least 2 chars)
            "(:[0-9]{1,5})?" +                                  // Optional port
            "(/.*)?$";                                          // Optional path

    public static boolean isValidUrl(final String url) {
        return (url != null) && url.matches(VALID_URL_REGEX);
    }
}
