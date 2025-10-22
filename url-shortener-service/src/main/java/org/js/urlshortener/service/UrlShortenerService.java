package org.js.urlshortener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.js.urlshortener.repository.UrlShortenerRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerService {
    private UrlShortenerRepository urlShortenerRepository;
}
