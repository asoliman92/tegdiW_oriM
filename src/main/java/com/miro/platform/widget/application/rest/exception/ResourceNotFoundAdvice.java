package com.miro.platform.widget.application.rest.exception;

import com.miro.platform.widget.application.rest.response.GenericJsonResponse;
import com.miro.platform.widget.domain.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ResourceNotFoundAdvice {

    @ResponseBody
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public GenericJsonResponse resourceNotFoundHandler(ResourceNotFoundException ex) {
        return new GenericJsonResponse(ex.getMessage());
    }
}
