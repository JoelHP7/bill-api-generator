package com.bill_api_generator.bill_api_generator.exception;

public class ClientNotFoundException extends ResourceNotFoundException {

    public ClientNotFoundException(Long id) {
        super("Client", id);
    }

    public ClientNotFoundException(String ref) {
        super("Client", "ref '" + ref + "'");
    }
}
