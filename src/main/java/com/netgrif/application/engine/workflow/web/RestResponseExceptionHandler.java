package com.netgrif.application.engine.workflow.web;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.Case;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException exception, HttpHeaders headers, HttpStatus status, WebRequest request) {
        try {
            List<JsonMappingException.Reference> path = ((JsonMappingException) exception.getCause()).getPath();
            JsonMappingException.Reference fieldReference = path.get(path.size() - 1);
            JsonMappingException.Reference caseReference = path.get(path.size() - 3);
            Field from = (Field) fieldReference.getFrom();
            Case useCase = (Case) caseReference.getFrom();

            log.error("[" + useCase.getStringId() + "] Could not parse value of field [" + from.getStringId() + "], value [" + from.getValue() + "]");
        } catch (Exception e) {
            log.warn("Unrecognized exception: ", e);
        }
        return super.handleHttpMessageNotWritable(exception, headers, status, request);
    }
}