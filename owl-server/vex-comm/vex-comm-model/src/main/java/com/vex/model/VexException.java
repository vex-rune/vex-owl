package com.vex.model;

public class VexException extends RuntimeException {

    private final String code;

    public VexException(String code, String message) {
        super(message);
        this.code = code;
    }

    public VexException(String message) {
        this("BUSINESS_ERROR", message);
    }

    public String getCode() {
        return code;
    }
}
