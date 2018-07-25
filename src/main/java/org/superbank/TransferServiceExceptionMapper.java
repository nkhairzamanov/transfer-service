package org.superbank;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class TransferServieExceptionMapper implements ExceptionMapper<RuntimeException> {
    @Override
    public Response toResponse(RuntimeException e) {
        return Response.serverError().entity(e).build();
    }
}
