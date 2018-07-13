package org.superbank;

public class TransferServiceRuntimeException extends RuntimeException {
    public TransferServiceRuntimeException(String message) {
        super(message);
    }

    public TransferServiceRuntimeException(Throwable t) {
        super(t);
    }
}
