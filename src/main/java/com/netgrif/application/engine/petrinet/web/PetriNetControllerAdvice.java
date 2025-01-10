package com.netgrif.application.engine.petrinet.web;

import com.netgrif.application.engine.workflow.domain.throwable.MissingProcessMetaDataException;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = {PetriNetController.class})
public class PetriNetControllerAdvice {

    @ExceptionHandler(MissingProcessMetaDataException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody
    MessageResource handleException(MissingProcessMetaDataException e) {
        log.error("Importing Petri net failed. " + e.getMessage(), e);
        return MessageResource.errorMessage(e.getMessage());
    }
}
