package com.bill_api_generator.bill_api_generator.exception;

public class DuplicateClientRefException extends DuplicateResourceException {

    public DuplicateClientRefException(String ref) {
        super("A client already exists with reference: " + ref);
    }
}
