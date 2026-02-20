package com.netgrif.application.engine.workflow.web;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@ControllerAdvice
public class RestResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestResponseExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        try {
            log.error("Received HttpMessageNotWritableException: {}", exception.getMessage(), exception);
            List<JsonMappingException.Reference> path = ((JsonMappingException) exception.getCause()).getPath();
            if (path.size() > 3) {
                JsonMappingException.Reference fieldReference = path.getLast();
                JsonMappingException.Reference caseReference = path.get(path.size() - 3);
                Field from = (Field) fieldReference.getFrom();
                Case useCase = (Case) caseReference.getFrom();
                log.error("[{}] Could not parse value of field [{}], value [{}]", useCase.getStringId(), from.getStringId(), from.getValue());
            }
        } catch (Exception e) {
            log.error("Unrecognized exception: ", e);
        }
        return super.handleHttpMessageNotWritable(exception, headers, status, request);
    }
}
