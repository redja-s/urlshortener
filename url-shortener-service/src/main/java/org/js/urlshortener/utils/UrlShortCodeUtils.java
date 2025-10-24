package org.js.urlshortener.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class UrlShortCodeUtils {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEFAULT_LENGTH = 6;
    private final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a random Base62 encoded short code
     * @return 6-character Base62 string
     */
    public String generateShortCode() {
        return generateShortCode(DEFAULT_LENGTH);
    }

    /**
     * Generates a random Base62 encoded short code of specified length
     * @param length desired length of the short code
     * @return Base62 string of specified length
     */
    public String generateShortCode(int length) {
        StringBuilder shortCode = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(BASE62_CHARS.length());
            shortCode.append(BASE62_CHARS.charAt(randomIndex));
        }

        return shortCode.toString();
    }
}
