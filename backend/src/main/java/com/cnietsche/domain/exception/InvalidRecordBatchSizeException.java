package com.cnietsche.domain.exception;

public class InvalidRecordBatchSizeException extends DomainException {

    public InvalidRecordBatchSizeException() {
        super("Count must be 50, 100, 400, or 1000");
    }
}
