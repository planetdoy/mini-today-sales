package com.okpos.todaysales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "에러 응답 형식")
public class ErrorResponse {
    
    @Schema(description = "성공 여부 (항상 false)", example = "false")
    private boolean success;
    
    @Schema(description = "에러 코드", example = "STORE_NOT_FOUND")
    private String errorCode;
    
    @Schema(description = "에러 메시지", example = "가맹점을 찾을 수 없습니다")
    private String message;
    
    @Schema(description = "요청 경로", example = "/api/v1/sales/webhook")
    private String path;
    
    @Schema(description = "에러 발생 시간", example = "2024-01-15T14:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "유효성 검증 오류 상세")
    private Map<String, String> validationErrors;
    
    @Schema(description = "추가 에러 상세")
    private List<String> details;
    
    @Schema(description = "추가 데이터")
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