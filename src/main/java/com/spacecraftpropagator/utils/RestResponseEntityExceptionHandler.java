package com.spacecraftpropagator.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler 
  extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value 
      = { IllegalArgumentException.class, IllegalStateException.class })
    protected ResponseEntity<Object> handleBadRequest(
      RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "Unexpected exception:  " + ex.getMessage();
        return handleExceptionInternal(ex, bodyOfResponse, 
          new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @ExceptionHandler(value 
            = { SimulationStepInvalidException.class, MomentOfInertiaInvalidException.class })
          protected ResponseEntity<Object> handlePredictorError(
            RuntimeException ex, WebRequest request) {
              String bodyOfResponse = "Error in predictor:  " + ex.getMessage();
              return handleExceptionInternal(ex, bodyOfResponse, 
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
          }
}
