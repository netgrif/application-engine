package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = {UserController.class})
public class UserControllerAdvice {

    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody
    MessageResource handleException(NumberFormatException e) {
        log.error("Long could not be parsed from request. " + e.getMessage(), e);
        return MessageResource.errorMessage(e.getMessage());
    }
}
