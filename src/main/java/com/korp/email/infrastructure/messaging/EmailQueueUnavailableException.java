package com.korp.email.infrastructure.messaging;

public class EmailQueueUnavailableException extends RuntimeException {

    public EmailQueueUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
