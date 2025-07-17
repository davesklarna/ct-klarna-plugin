package com.e2x.klarnact.exception;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.core.Response;

@RequiredArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@RegisterForReflection
public class ExceptionResponse {
    private final Response.Status status;

    public int getStatusCode() {
        return status.getStatusCode();
    }

    public String getStatusDescription() {
        return status.getReasonPhrase();
    }

    @Getter
    private final String message;
}
