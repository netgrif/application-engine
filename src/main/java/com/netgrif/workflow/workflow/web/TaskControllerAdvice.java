package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {TaskController.class})
public class TaskControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    @ExceptionHandler(IllegalTaskStateException.class)
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    public @ResponseBody
    MessageResource handleException(IllegalTaskStateException e) {
        log.error("Task event authorization failed. " + e.getMessage(), e);
        return MessageResource.errorMessage(e.getMessage());
    }
}
