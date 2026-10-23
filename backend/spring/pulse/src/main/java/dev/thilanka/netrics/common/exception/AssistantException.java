package dev.thilanka.netrics.common.exception;

import org.springframework.http.HttpStatus;

public class AssistantException extends BaseException {
    public AssistantException(HttpStatus status, String errorCode, String message) {
        super(message, errorCode, status);
    }
}
