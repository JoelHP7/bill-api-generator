package com.bill_api_generator.bill_api_generator.exception;

public class ClientNotDeletedException extends ResourceStateException {

    public ClientNotDeletedException(Long id) {
        super("Client with id " + id + " is not deleted and cannot be restored");
    }
}
