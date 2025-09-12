package com.okpos.todaysales.exception;

/**
 * 가맹점을 찾을 수 없을 때 발생하는 예외
 */
public class StoreNotFoundException extends RuntimeException {
    
    private final String businessNumber;
    
    public StoreNotFoundException(String businessNumber) {
        super("가맹점을 찾을 수 없습니다: " + businessNumber);
        this.businessNumber = businessNumber;
    }
    
    public StoreNotFoundException(String businessNumber, String message) {
        super(message);
        this.businessNumber = businessNumber;
    }
    
    public StoreNotFoundException(String businessNumber, String message, Throwable cause) {
        super(message, cause);
        this.businessNumber = businessNumber;
    }
    
    public String getBusinessNumber() {
        return businessNumber;
    }
}