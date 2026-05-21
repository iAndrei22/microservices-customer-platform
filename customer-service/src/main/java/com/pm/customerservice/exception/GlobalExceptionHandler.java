package com.pm.customerservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(
            GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationsExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler (EmailAlreadyExistsException.class)
        public ResponseEntity<Map<String,String>> handleEmailAlreadyExistsException(
                EmailAlreadyExistsException ex){

        log.warn("Email address already exists {}", ex.getMessage());
            Map<String,String> errors = new HashMap<>();
            errors.put("message", "Email address already exists");

            return ResponseEntity.badRequest().body(errors);
        }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCustomerNotFoundException(
            CustomerNotFoundException ex) {
        log.warn("Customer not found {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Customer not found");
        return ResponseEntity.badRequest().body(errors);
    }
}
