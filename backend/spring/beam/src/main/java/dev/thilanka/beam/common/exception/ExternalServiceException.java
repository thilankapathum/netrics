package dev.thilanka.beam.common.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BaseException {
    public ExternalServiceException(String serviceName, String message) {
        super(
                String.format("External service '%s' error: '%s'", serviceName, message),
                "EXTERNAL_SERVICE_ERROR",
                HttpStatus.BAD_GATEWAY
        );
    }

    public ExternalServiceException(String serviceName, Throwable cause) {
        super(
                String.format("External service '%s' is unavailable", serviceName),
                cause,
                "EXTERNAL_SERVICE_ERROR",
                HttpStatus.BAD_GATEWAY
        );
    }
}
