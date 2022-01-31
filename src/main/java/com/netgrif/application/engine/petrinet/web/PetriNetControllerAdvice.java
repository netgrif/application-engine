package com.netgrif.application.engine.petrinet.web;

import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {PetriNetController.class})
public class PetriNetControllerAdvice {

    public static final Logger log = LoggerFactory.getLogger(PetriNetController.class);

    @ExceptionHandler(MissingPetriNetMetaDataException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody
    MessageResource handleException(MissingPetriNetMetaDataException e) {
        log.error("Importing Petri net failed. " + e.getMessage(), e);
        return MessageResource.errorMessage(e.getMessage());
    }
}
