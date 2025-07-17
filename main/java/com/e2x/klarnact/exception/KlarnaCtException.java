package com.e2x.klarnact.exception;

import lombok.Getter;

import javax.ws.rs.core.Response;

@Getter
public class KlarnaCtException extends RuntimeException {
    private final Response.Status status;

    public KlarnaCtException(String message) {
        super(message);
        this.status = Response.Status.BAD_REQUEST;
    }

    public KlarnaCtException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    @Override
    public String toString() {
        return "KlarnaCtException{" +
                "message=" + getMessage() + ", " +
                "status=" + status +
                '}';
    }
}
