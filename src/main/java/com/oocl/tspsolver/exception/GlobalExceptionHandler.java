package com.oocl.tspsolver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmptyOptimizedOrderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleEmptyTextException(Exception e) {
        return e.getMessage();
    }

    @ExceptionHandler(AmapDrivingApiException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAmapDrivingApiException(Exception e) {
        return e.getMessage();
    }
}
