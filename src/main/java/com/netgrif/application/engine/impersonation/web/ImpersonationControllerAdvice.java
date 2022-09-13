package com.netgrif.application.engine.impersonation.web;

import com.netgrif.application.engine.impersonation.exceptions.IllegalImpersonationAttemptException;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.impersonation.web.responsebodies.ImpersonationNotAvailableResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = ImpersonationController.class)
public class ImpersonationControllerAdvice {

    @ExceptionHandler
    public ResponseEntity handleException(IllegalImpersonationAttemptException ex) {
        log.error("Illegal attempt at impersonation", ex);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ImpersonationNotAvailableResponse handleException(ImpersonatedUserHasSessionException ex) {
        log.error("User is already logged", ex);
        return new ImpersonationNotAvailableResponse(ex.isImpersonated());
    }

}
