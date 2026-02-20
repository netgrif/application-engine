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
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        try {
            Throwable cause = exception.getCause();
            if (!(cause instanceof JsonMappingException jme)) {
                log.error("Received HttpMessageNotWritableException: {}", exception.getMessage(), exception);
                return super.handleHttpMessageNotWritable(exception, headers, status, request);
            }

            List<JsonMappingException.Reference> path = jme.getPath();
            if (path.size() > 3) {
                Object fieldFrom = path.getLast().getFrom();
                Object caseFrom = path.get(path.size() - 3).getFrom();

                if (fieldFrom instanceof Field field && caseFrom instanceof Case useCase) {
                    log.debug("[{}] Could not parse value of field [{}], value [{}] | path={}",
                            useCase.getStringId(), field.getStringId(), field.getValue(), jme.getPathReference());
                } else {
                    log.error("JSON write failed: {} | path={}",
                            jme.getOriginalMessage(), jme.getPathReference(), jme);
                }
            } else {
                log.error("JSON write failed: {} | path={}",
                        jme.getOriginalMessage(), jme.getPathReference(), jme);
            }

        } catch (Exception e) {
            log.error("Unrecognized exception: ", e);
        }
        return super.handleHttpMessageNotWritable(exception, headers, status, request);
    }
}