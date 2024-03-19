package com.findear.main;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllException(Exception e) {
        e.printStackTrace();
        return ResponseEntity
                .internalServerError()
                .body("CommonControllerAdvice 실행됨");
    }
}
