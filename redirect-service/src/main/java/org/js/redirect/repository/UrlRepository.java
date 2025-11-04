package org.js.redirect.repository;

import org.js.redirect.persistence.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
    Optional<UrlEntity> findByShortCode(final String shortCode);

    void deleteByShortCode(final String shortCode);
}
