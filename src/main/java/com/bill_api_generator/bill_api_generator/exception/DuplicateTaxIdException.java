package com.bill_api_generator.bill_api_generator.exception;

public class DuplicateTaxIdException extends DuplicateResourceException {

    public DuplicateTaxIdException(String taxId) {
        super("A client already exists with tax ID: " + taxId);
    }
}
