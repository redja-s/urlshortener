package org.js.urlshortener.utils;

import java.security.SecureRandom;

public class UrlShortCodeUtils {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEFAULT_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a random Base62 encoded short code
     * @return 6-character Base62 string
     */
    public static String generateShortCode() {
        return generateShortCode(DEFAULT_LENGTH);
    }

    /**
     * Generates a random Base62 encoded short code of specified length
     * @param length desired length of the short code
     * @return Base62 string of specified length
     */
    public static String generateShortCode(int length) {
        StringBuilder shortCode = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(BASE62_CHARS.length());
            shortCode.append(BASE62_CHARS.charAt(randomIndex));
        }

        return shortCode.toString();
    }

    /**
     * Generates a Base62 encoded short code from a long value (e.g., database ID)
     * @param value the long value to encode
     * @return Base62 encoded string
     */
    public static String encodeToBase62(long value) {
        if (value == 0) {
            return "0";
        }

        StringBuilder encoded = new StringBuilder();

        while (value > 0) {
            encoded.append(BASE62_CHARS.charAt((int) (value % 62)));
            value /= 62;
        }

        return encoded.reverse().toString();
    }

    /**
     * Decodes a Base62 string back to a long value
     * @param encoded Base62 encoded string
     * @return decoded long value
     */
    public static long decodeFromBase62(String encoded) {
        long decoded = 0;
        long multiplier = 1;

        for (int i = encoded.length() - 1; i >= 0; i--) {
            char c = encoded.charAt(i);
            int value = BASE62_CHARS.indexOf(c);
            if (value == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            decoded += value * multiplier;
            multiplier *= 62;
        }

        return decoded;
    }
}
