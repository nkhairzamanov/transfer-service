package org.superbank;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class TransferServiceExceptionMapper implements ExceptionMapper<TransferServiceRuntimeException> {
    @Override
    public Response toResponse(TransferServiceRuntimeException e) {
        return Response.serverError().entity(e.getMessage()).build();
    }
}
