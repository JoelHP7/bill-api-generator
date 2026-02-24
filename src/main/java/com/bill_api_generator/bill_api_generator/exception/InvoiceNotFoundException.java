package com.bill_api_generator.bill_api_generator.exception;

public class InvoiceNotFoundException extends ResourceNotFoundException {

    public InvoiceNotFoundException(Long id) {
        super("Invoice", id);
    }
}
