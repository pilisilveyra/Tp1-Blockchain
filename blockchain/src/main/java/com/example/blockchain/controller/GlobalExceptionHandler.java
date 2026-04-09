package com.example.blockchain.controller;

import com.example.blockchain.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e) {
        return buildBadRequestFromMessage(e.getMessage(), "INVALID_REQUEST");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException e) {
        return buildBadRequestFromMessage(e.getMessage(), "INVALID_STATE");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception e) {
        e.printStackTrace();

        ApiError error = new ApiError(
                "error",
                new ApiError.ErrorDetail("INTERNAL_ERROR", "Error interno del servidor")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ResponseEntity<ApiError> buildBadRequestFromMessage(String rawMessage, String defaultCode) {
        String code = defaultCode;
        String message = rawMessage != null ? rawMessage : "Error";

        if (rawMessage != null && rawMessage.contains(":")) {
            String[] parts = rawMessage.split(":", 2);
            code = parts[0].trim();
            message = parts[1].trim();
        }

        ApiError error = new ApiError(
                "error",
                new ApiError.ErrorDetail(code, message)
        );

        return ResponseEntity.badRequest().body(error);
    }
}