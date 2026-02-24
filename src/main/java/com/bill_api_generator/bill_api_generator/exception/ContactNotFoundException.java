package com.bill_api_generator.bill_api_generator.exception;

public class ContactNotFoundException extends ResourceNotFoundException {

    public ContactNotFoundException(Long id) {
        super("Contact", id);
    }
}
