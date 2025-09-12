package com.okpos.todaysales.exception;

/**
 * 정산 처리 중 발생하는 예외
 */
public class SettlementException extends RuntimeException {
    
    private final String errorCode;
    private final Object additionalData;
    
    public SettlementException(String message) {
        super(message);
        this.errorCode = "SETTLEMENT_ERROR";
        this.additionalData = null;
    }
    
    public SettlementException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.additionalData = null;
    }
    
    public SettlementException(String errorCode, String message, Object additionalData) {
        super(message);
        this.errorCode = errorCode;
        this.additionalData = additionalData;
    }
    
    public SettlementException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SETTLEMENT_ERROR";
        this.additionalData = null;
    }
    
    public SettlementException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.additionalData = null;
    }
    
    public SettlementException(String errorCode, String message, Object additionalData, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.additionalData = additionalData;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object getAdditionalData() {
        return additionalData;
    }
}