package com.bill_api_generator.bill_api_generator.exception;

public abstract class BillApiException extends RuntimeException {

    protected BillApiException(String message) {
        super(message);
    }

    protected BillApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
