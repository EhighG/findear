package com.findear.main.common.exception;

import com.findear.main.common.response.FailResponse;
import com.findear.main.message.common.exception.MessageException;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllException(Exception e) {

        log.info("공용 exception 발생");

        return ResponseEntity.badRequest().body(new FailResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwtException(ExpiredJwtException e) {

        log.info("jwt 토큰 만료 exception 발생");

        return ResponseEntity.badRequest().body(new FailResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
    }
    @ExceptionHandler(MessageException.class)
    public ResponseEntity<?> handleMessageException(Exception e) {

        log.info("messageException 발생");

        return ResponseEntity.badRequest().body(new FailResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

}