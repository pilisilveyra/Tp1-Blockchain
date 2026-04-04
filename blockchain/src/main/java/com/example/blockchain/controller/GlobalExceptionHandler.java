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
        String message = e.getMessage();
        String code = "INVALID_REQUEST";

        if (message != null && message.contains(":")) {
            String[] parts = message.split(":", 2);
            code = parts[0];
            message = parts[1].trim();
        }

        ApiError error = new ApiError(
                "error",
                new ApiError.ErrorDetail(code, message)
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException e
    ) {
        ApiError error = new ApiError(
                "error",
                new ApiError.ErrorDetail("INVALID_STATE", e.getMessage())
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception e
    ) {
        ApiError error = new ApiError(
                "error",
                new ApiError.ErrorDetail("INTERNAL_ERROR", "Error interno del servidor")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }


}
