package com.retailhub.ordermanagementservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidUserException.class)
    public ResponseEntity<ExceptionInfoModel> invalidUserExceptionHandler(HttpServletRequest request, InvalidUserException exception) {
        return new ResponseEntity<>(createExceptionInfoModel(request, exception), HttpStatus.NOT_FOUND);
    }

    private ExceptionInfoModel createExceptionInfoModel(HttpServletRequest request, Exception exception) {
        return new ExceptionInfoModel(request.getRequestURI(), exception);
    }
}
