package com.example.blockchain.controller;

import com.example.blockchain.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument( IllegalArgumentException e,
                                                           HttpServletRequest request) {
        ApiError error = new ApiError(
                e.getMessage(),
                System.currentTimeMillis(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException e,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                e.getMessage(),
                System.currentTimeMillis(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception e,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                "Error interno del servidor",
                System.currentTimeMillis(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }


}
