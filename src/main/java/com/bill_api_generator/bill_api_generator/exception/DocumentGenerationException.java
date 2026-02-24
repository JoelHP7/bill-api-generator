package com.bill_api_generator.bill_api_generator.exception;

public class DocumentGenerationException extends BillApiException {

    public DocumentGenerationException(String invoiceNumber, Throwable cause) {
        super("Failed to generate document for invoice: " + invoiceNumber, cause);
    }

    public DocumentGenerationException(String message) {
        super(message);
    }
}
