package com.okpos.todaysales.exception;

import com.okpos.todaysales.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("유효성 검증 실패: {}", errors);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                "VALIDATION_ERROR",
                "입력값 검증에 실패했습니다",
                request.getDescription(false).replace("uri=", ""),
                errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }
        
        log.warn("제약조건 위반: {}", errors);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                "CONSTRAINT_VIOLATION",
                "입력값 검증에 실패했습니다",
                request.getDescription(false).replace("uri=", ""),
                errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        String message = String.format("'%s' 파라미터의 값 '%s'이(가) 올바르지 않습니다", 
                ex.getName(), ex.getValue());
        
        log.warn("타입 불일치 오류: {}", message);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                "TYPE_MISMATCH",
                message,
                request.getDescription(false).replace("uri=", ""),
                Map.of("parameter", ex.getName(), "value", String.valueOf(ex.getValue()))
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("잘못된 인자 오류: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                "ILLEGAL_ARGUMENT",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStoreNotFoundException(
            StoreNotFoundException ex, WebRequest request) {
        
        log.warn("가맹점 조회 실패: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                "STORE_NOT_FOUND",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                Map.of("businessNumber", ex.getBusinessNumber())
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(
            InvalidRequestException ex, WebRequest request) {
        
        log.warn("잘못된 요청: {}", ex.getMessage());
        
        Map<String, Object> additionalData = new HashMap<>();
        if (ex.getField() != null) {
            additionalData.put("field", ex.getField());
        }
        if (ex.getValue() != null) {
            additionalData.put("value", ex.getValue());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
                "INVALID_REQUEST",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                additionalData.isEmpty() ? null : additionalData
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(SettlementException.class)
    public ResponseEntity<ErrorResponse> handleSettlementException(
            SettlementException ex, WebRequest request) {
        
        log.error("정산 처리 오류: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getAdditionalData()
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("예상치 못한 오류 발생", ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다",
                request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}