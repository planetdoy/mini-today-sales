package com.okpos.todaysales.exception;

/**
 * 잘못된 요청 데이터로 인해 발생하는 예외
 */
public class InvalidRequestException extends RuntimeException {
    
    private final String field;
    private final Object value;
    
    public InvalidRequestException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }
    
    public InvalidRequestException(String field, Object value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.value = null;
    }
    
    public InvalidRequestException(String field, Object value, String message, Throwable cause) {
        super(message, cause);
        this.field = field;
        this.value = value;
    }
    
    public String getField() {
        return field;
    }
    
    public Object getValue() {
        return value;
    }
}