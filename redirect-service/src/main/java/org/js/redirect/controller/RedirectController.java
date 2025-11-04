package org.js.redirect.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.js.redirect.service.RedirectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RedirectController {
    private final RedirectService redirectService;

    /**
     * Redirect to the original long URL.
     * Returns 301 (Permanent Redirect) for browser/CDN caching.
     *
     * @param shortCode The short code from the URL path
     * @param response HTTP response object
     * @throws IOException if redirect fails
     */
    @GetMapping("/{shortCode}")
    public void redirect(
            @PathVariable String shortCode,
            HttpServletResponse response
    ) throws IOException {
        log.info("Redirecting short code: {}", shortCode);

        String longUrl = redirectService.getLongUrl(shortCode);

        // Add protocol if missing
        if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://")) {
            longUrl = "https://" + longUrl;
        }

        // 301 = Permanent redirect (cacheable by browsers/CDN)
        // Use 302 if you need to track every click (not cached)
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.setHeader("Location", longUrl);
        response.setHeader("Cache-Control", "public, max-age=3600"); // Cache for 1 hour

        log.debug("Redirected {} to {}", shortCode, longUrl);
    }
}
