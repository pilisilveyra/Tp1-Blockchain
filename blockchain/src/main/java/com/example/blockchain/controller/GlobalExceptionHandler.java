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
            code = parts[0].trim();
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
        String message = e.getMessage();
        String code = "INVALID_REQUEST";
        if (message != null) {
            String normalized = message.toLowerCase();
            if (normalized.contains("block")
                    || normalized.contains("bloque")
                    || normalized.contains("blockchain")
                    || normalized.contains("chain")
                    || normalized.contains("cadena")) {
                code = "INVALID_BLOCK";
            }
        }
        ApiError error = new ApiError(
                "error",
                new ApiError.ErrorDetail(code, message)
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception e) {
        e.printStackTrace();

        ApiError error = new ApiError(
                "error",
                new ApiError.ErrorDetail("INTERNAL_ERROR", e.getMessage())
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException e) {
        e.printStackTrace();

        ApiError error = new ApiError(
                "error",
                new ApiError.ErrorDetail("RUNTIME_ERROR", e.getMessage())
        );

        return ResponseEntity.badRequest().body(error);
    }

}
