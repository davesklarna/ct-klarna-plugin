package com.e2x.klarnact.exception;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.ws.rs.core.Response;

@Slf4j
class ExceptionMapper {
    @ServerExceptionMapper
    public Response mapException(KlarnaCtException ex) {
        log.error(ex.getMessage(), ex);
        return Response.status(ex.getStatus())
                .entity(new ExceptionResponse(ex.getStatus(), ex.getMessage()))
                .build();
    }
}
