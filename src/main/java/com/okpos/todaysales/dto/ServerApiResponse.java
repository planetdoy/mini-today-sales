package com.okpos.todaysales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "API 공통 응답 형식")
public class ServerApiResponse<T> {
    
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "응답 메시지", example = "성공")
    private String message;
    
    @Schema(description = "응답 데이터")
    private T data;
    
    @Schema(description = "응답 시간", example = "2024-01-15T14:30:00")
    private LocalDateTime timestamp;
    
    public static <T> ServerApiResponse<T> success(T data) {
        return ServerApiResponse.<T>builder()
                .success(true)
                .message("성공")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ServerApiResponse<T> success(String message, T data) {
        return ServerApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ServerApiResponse<T> error(String message) {
        return ServerApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ServerApiResponse<T> error(String message, T data) {
        return ServerApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}