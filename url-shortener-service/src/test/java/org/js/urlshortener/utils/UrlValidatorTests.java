package org.js.urlshortener.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UrlValidatorTests {

    @ParameterizedTest
    @ValueSource(strings = {
            "example.com",
            "www.example.com",
            "test.org",
            "sub.domain.co.uk",
            "example.io",
            "test-site.com",
            "my_site.org",
            "a.co",
            "123.com",
            "test123.example.org"
    })
    void test_validUrls_withoutProtocol_returnsTrue(String url) {
        assertTrue(UrlValidator.isValidUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com",
            "https://example.com",
            "http://www.example.com",
            "https://www.google.com",
            "http://sub.domain.co.uk",
            "https://test-site.com",
            "http://my_site.org"
    })
    void test_validUrls_withProtocol_returnsTrue(String url) {
        assertTrue(UrlValidator.isValidUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "example.com:8080",
            "api.service.com:443",
            "test.org:80",
            "example.com:65535"
    })
    void test_validUrls_withPort_returnsTrue(String url) {
        assertTrue(UrlValidator.isValidUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "example.com/path",
            "www.site.org/api/v1/users",
            "test.com/path/to/resource?param=value",
            "example.org/test#section",
            "site.com/path/with-dashes_and_underscores",
            "api.com/v1/users/123/profile"
    })
    void test_validUrls_withPath_returnsTrue(String url) {
        assertTrue(UrlValidator.isValidUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com:8080/api/v1",
            "http://sub.domain.co.uk:3000/path",
            "https://test-site.org/users?id=123#top"
    })
    void test_validUrls_fullUrls_returnsTrue(String url) {
        assertTrue(UrlValidator.isValidUrl(url));
    }

    @Test
    void test_nullUrl_returnsFalse() {
        assertFalse(UrlValidator.isValidUrl(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "notavalidurl",
            "just-text",
            "ftp://example.com",
            "mailto:user@example.com",
            "file:///path/to/file"
    })
    void test_invalidUrls_invalidFormat_returnsFalse(String url) {
        assertFalse(UrlValidator.isValidUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ".example.com",
            "example.",
            "example..com",
            "test.org....com",
            "site.com.",
            "..example.com",
            "example.com.."
    })
    void test_invalidUrls_invalidDots_returnsFalse(String url) {
        assertFalse(UrlValidator.isValidUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "example.c",
            "test.x",
            "site.a"
    })
    void test_invalidUrls_shortTld_returnsFalse(String url) {
        assertFalse(UrlValidator.isValidUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "example.com:99999",
            "test.org:123456",
            "site.com:0"
    })
    void test_invalidUrls_invalidPort_returnsFalse(String url) {
        assertFalse(UrlValidator.isValidUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-example.com",
            "example-.com",
            "_example.com",
            "example.com-",
            "test.-com",
            "-test.com"
    })
    void test_invalidUrls_invalidStartEnd_returnsFalse(String url) {
        assertFalse(UrlValidator.isValidUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://",
            "https://",
            "http://.com",
            "https://.org"
    })
    void test_invalidUrls_protocolOnly_returnsFalse(String url) {
        assertFalse(UrlValidator.isValidUrl(url));
    }

    @Test
    void test_complexValidUrl_returnsTrue() {
        String complexUrl = "https://api-v2.my-service_name.co.uk:8443/api/v1/users/123/profile?include=details&format=json#section-1";
        assertTrue(UrlValidator.isValidUrl(complexUrl));
    }

    @Test
    void test_edgeCase_singleCharacterDomain_returnsTrue() {
        assertTrue(UrlValidator.isValidUrl("a.co"));
        assertTrue(UrlValidator.isValidUrl("x.io"));
    }

    @Test
    void test_edgeCase_numbersInDomain_returnsTrue() {
        assertTrue(UrlValidator.isValidUrl("123.com"));
        assertTrue(UrlValidator.isValidUrl("test123.example456.org"));
    }
}
