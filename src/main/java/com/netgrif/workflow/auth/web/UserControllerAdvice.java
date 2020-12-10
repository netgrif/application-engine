package com.netgrif.workflow.auth.web;

import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {UserController.class})
public class UserControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody
    MessageResource handleException(NumberFormatException e) {
        log.error("Long could not be parsed from request. " + e.getMessage(), e);
        return MessageResource.errorMessage(e.getMessage());
    }
}
