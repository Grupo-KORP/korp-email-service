package com.korp.email.infrastructure.web;

import com.korp.email.infrastructure.messaging.EmailQueueUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EmailQueueExceptionHandler {

    @ExceptionHandler(EmailQueueUnavailableException.class)
    public ResponseEntity<Void> handleEmailQueueUnavailable(EmailQueueUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
