package org.js.urlshortener.repository;

import org.js.urlshortener.persistence.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlShortenerRepository extends JpaRepository<UrlEntity, Long> {
}
