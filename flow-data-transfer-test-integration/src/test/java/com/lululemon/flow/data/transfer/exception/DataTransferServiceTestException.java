package com.lululemon.flow.data.transfer.exception;

public class DataTransferServiceTestException extends RuntimeException {
    private static final long serialVersionUID = 7963242442838360150L;

    public DataTransferServiceTestException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DataTransferServiceTestException(final String message) {
        super(message);
    }
}
