package org.js.redirect.exception;

import lombok.extern.slf4j.Slf4j;
import org.js.redirect.exception.model.GenericErrorResponse;
import org.js.redirect.exception.model.UrlExpiredException;
import org.js.redirect.exception.model.UrlNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation failed: {}", errors);

        GenericErrorResponse errorResponse = GenericErrorResponse.builder()
                .message("Invalid URL")
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<GenericErrorResponse> handleUrlNotFoundException() {
        log.warn("URL Entity not found");

        GenericErrorResponse errorResponse = GenericErrorResponse.builder()
                .message("No URL found")
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<GenericErrorResponse> handleUrlExpiredException() {
        GenericErrorResponse errorResponse = GenericErrorResponse.builder()
                .message("URL Expired")
                .build();

        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(errorResponse);
    }
}
