package com.okpos.todaysales.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    private boolean success;
    private String errorCode;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private Map<String, String> validationErrors;
    private List<String> details;
    private Object additionalData;
    
    public static ErrorResponse of(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(String errorCode, String message, String path, Map<String, String> validationErrors) {
        return ErrorResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(String errorCode, String message, String path, List<String> details) {
        return ErrorResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(String errorCode, String message, String path, Object additionalData) {
        return ErrorResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .additionalData(additionalData)
                .timestamp(LocalDateTime.now())
                .build();
    }
}