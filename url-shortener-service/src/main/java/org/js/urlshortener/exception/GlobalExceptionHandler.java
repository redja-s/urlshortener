package org.js.urlshortener.exception;

import lombok.extern.slf4j.Slf4j;
import org.js.urlshortener.exception.model.GenericErrorResponse;
import org.js.urlshortener.exception.model.InvalidUrlException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<GenericErrorResponse> handleInvalidUrlException() {
        log.warn("Invalid URL detected");

        GenericErrorResponse errorResponse = GenericErrorResponse.builder()
                .message("Not a valid URL")
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}
