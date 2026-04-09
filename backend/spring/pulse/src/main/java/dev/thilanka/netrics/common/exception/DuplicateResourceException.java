package dev.thilanka.netrics.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                "DUPLICATE_RESOURCE",
                HttpStatus.CONFLICT,
                new Object[]{resourceName, fieldName, fieldValue}
        );
    }
}
