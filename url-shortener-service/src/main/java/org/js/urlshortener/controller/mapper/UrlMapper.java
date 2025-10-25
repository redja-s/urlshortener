package org.js.urlshortener.controller.mapper;

import org.js.urlshortener.controller.model.PostUrlShortenRequest;
import org.js.urlshortener.controller.model.ShortenResponse;
import org.js.urlshortener.persistence.entity.UrlEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface UrlMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shortCode", source = "shortCode")
    @Mapping(target = "longUrl", source = "request.url")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "expiresAt", source = "expiresAt")
    UrlEntity mapToUrlEntity(PostUrlShortenRequest request, String shortCode, LocalDateTime createdAt, LocalDateTime expiresAt);

    @Mapping(target = "shortCode", source = "entity.shortCode")
    @Mapping(target = "originalUrl", source = "entity.longUrl")
    @Mapping(target = "expiresAt", source = "entity.expiresAt")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    ShortenResponse mapUrlEntityToResponse(UrlEntity entity);
}
