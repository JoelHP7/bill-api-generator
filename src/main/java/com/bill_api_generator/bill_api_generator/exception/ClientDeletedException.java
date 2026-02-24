package com.bill_api_generator.bill_api_generator.exception;

public class ClientDeletedException extends ResourceStateException {

    public ClientDeletedException(Long id) {
        super("Client with id " + id + " has been deleted and cannot be accessed");
    }
}
