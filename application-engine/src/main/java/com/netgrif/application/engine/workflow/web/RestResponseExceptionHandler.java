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
            Throwable cause = exception.getCause();
            if (!(cause instanceof JsonMappingException jme)) {
                log.error("Received HttpMessageNotWritableException: {}", exception.getMessage(), exception);
                return super.handleHttpMessageNotWritable(exception, headers, status, request);
            }

            List<JsonMappingException.Reference> path = jme.getPath();

            if (log.isTraceEnabled()) {
                log.trace("JSON write failed (cause). msg={} | pathRef={}",
                        jme.getOriginalMessage(),
                        jme.getPathReference(),
                        jme);

                tracePathAll(path);
            }

            if (log.isDebugEnabled()) {
                for (int i = 0; i < path.size(); i++) {
                    log.debug("Reference[{}]: {}", i, path.get(i));
                }
            }

            if (path.size() > 3) {
                Object fieldFrom = path.getLast().getFrom();
                log.debug("Field of class [{}] from: {}", fieldFrom == null ? "null" : fieldFrom.getClass(), fieldFrom);
                Object caseFrom = path.get(path.size() - 3).getFrom();
                log.debug("Case of class [{}] from: {}", caseFrom == null ? "null" : caseFrom.getClass(), caseFrom);

                if (fieldFrom instanceof Field field && caseFrom instanceof Case useCase) {
                    log.debug("[{}] Could not parse value of field [{}], value [{}] | path={}",
                            useCase.getStringId(), field.getStringId(), field.getValue(), jme.getPathReference());
                } else {
                    log.error("JSON write failed: {} | path={} | details={}",
                            jme.getOriginalMessage(), jme.getPathReference(), describePath(path), jme);
                }
            } else {
                log.error("JSON write failed: {} | path={} | details={}",
                        jme.getOriginalMessage(), jme.getPathReference(), describePath(path), jme);
            }

        } catch (Exception e) {
            log.error("Unrecognized exception: ", e);
        }
        return super.handleHttpMessageNotWritable(exception, headers, status, request);
    }

    private static String describePath(List<JsonMappingException.Reference> path) {
        if (path == null || path.isEmpty()) return "<empty>";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            JsonMappingException.Reference ref = path.get(i);
            Object from = ref.getFrom();

            if (i > 0) sb.append(" | ");

            sb.append(i).append(":");
            sb.append(from == null ? "null" : from.getClass().getSimpleName());

            if (ref.getFieldName() != null) sb.append(".").append(ref.getFieldName());
            if (ref.getIndex() >= 0) sb.append("[").append(ref.getIndex()).append("]");

            sb.append(" (").append(ref).append(")");
        }
        return sb.toString();
    }

    private static void tracePathAll(List<JsonMappingException.Reference> path) {
        if (path == null || path.isEmpty()) {
            log.trace("[JSON_WRITE][PATH] <empty>");
            return;
        }

        for (int i = 0; i < path.size(); i++) {
            JsonMappingException.Reference ref = path.get(i);
            Object from = ref.getFrom();

            String where = (ref.getFieldName() != null ? "." + ref.getFieldName() : "")
                    + (ref.getIndex() >= 0 ? "[" + ref.getIndex() + "]" : "");

            log.trace("[JSON_WRITE][PATH] idx={} fromType={}{} ref={} from={}",
                    i,
                    (from == null ? "null" : from.getClass().getName()),
                    where,
                    ref,
                    from);
        }
    }
}