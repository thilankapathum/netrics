package dev.thilanka.beam.common.exception;

import org.springframework.http.HttpStatus;

public class DataQueryException extends BaseException {
    public DataQueryException(String message) {
        super(message, "DATABASE_QUERY_FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
