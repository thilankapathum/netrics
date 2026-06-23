package dev.thilanka.beam.common.exception;

import org.springframework.http.HttpStatus;

public class DataNotChangedException extends BaseException {

    public DataNotChangedException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s already exists %s: '%s'", resourceName, fieldName, fieldValue),
                "DATA_NOT_CHANGED",
                HttpStatus.NOT_MODIFIED,
                new Object[]{resourceName, fieldName, fieldValue}
        );
    }

    public DataNotChangedException(String message) {
        super(message, "DATA_NOT_CHANGED", HttpStatus.NOT_MODIFIED);
    }
}
