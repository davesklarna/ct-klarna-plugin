package com.e2x.klarnact.exception;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class NotFoundException extends KlarnaCtException {

    public NotFoundException(String message) {
        super(message, NOT_FOUND);
    }
}
