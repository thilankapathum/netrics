package dev.thilanka.beam.common.exception;

import org.springframework.http.HttpStatus;

public class FileProcessingException extends BaseException {
    public FileProcessingException(String message, Throwable cause) {

        super(
                message,
                cause,
                "FILE_PROCESSING_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
