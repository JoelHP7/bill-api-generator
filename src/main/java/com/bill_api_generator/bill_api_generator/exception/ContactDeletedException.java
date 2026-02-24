package com.bill_api_generator.bill_api_generator.exception;

public class ContactDeletedException extends ResourceStateException {

    public ContactDeletedException(Long id) {
        super("Contact with id " + id + " has been deleted and cannot be accessed");
    }
}
