package com.cmips.dto;

public class ValidationError {

    private String errorCode;
    private String message;

    public ValidationError() {}

    public ValidationError(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
