// Copyright (C) 2024, M. Yang 
// 
//     This program is free software: you can redistribute it and/or modify
//     it under the terms of the GNU General Public License as published by
//     the Free Software Foundation, either version 3 of the License, or
//     (at your option) any later version.
// 
//     This program is distributed in the hope that it will be useful, 
//     but WITHOUT ANY WARRANTY; without even the implied warranty of
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
//     GNU General Public License for more details.
// 
//     For further details, please see the README.md file included
//     with this software, and/or the GNU General Public License html
//     file which is also included with this software.

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
