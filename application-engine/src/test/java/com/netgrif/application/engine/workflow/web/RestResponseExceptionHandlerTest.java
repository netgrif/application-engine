package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import tools.jackson.core.JacksonException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestResponseExceptionHandlerTest {

    private final TestHandler handler = new TestHandler();

    @Test
    void handlesNonJacksonWriteFailureWithDefaultResponse() {
        ResponseEntity<Object> response = handler.handle(new HttpMessageNotWritableException("plain failure"));

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertWritableFailureBody(response);
    }

    @Test
    void handlesJacksonWriteFailureWithShortPath() {
        TestJacksonException cause = new TestJacksonException("json failure");
        cause.prependPath("root", "data");

        ResponseEntity<Object> response = handler.handle(new HttpMessageNotWritableException("json failure", cause));

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertWritableFailureBody(response);
    }

    @Test
    void handlesJacksonWriteFailureForCaseFieldPath() {
        Case useCase = mock(Case.class);
        when(useCase.getStringId()).thenReturn("case-1");
        TextField field = new TextField();
        field.setImportId("text");
        field.setValue("broken");
        TestJacksonException cause = new TestJacksonException("json failure");
        cause.prependPath(field, "value");
        cause.prependPath(new Object(), "dataSet");
        cause.prependPath(useCase, "case");
        cause.prependPath(new Object(), "root");

        ResponseEntity<Object> response = handler.handle(new HttpMessageNotWritableException("json failure", cause));

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertWritableFailureBody(response);
    }

    private static void assertWritableFailureBody(ResponseEntity<Object> response) {
        ProblemDetail body = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.getStatus());
        assertEquals("Failed to write request", body.getDetail());
    }

    static class TestHandler extends RestResponseExceptionHandler {
        ResponseEntity<Object> handle(HttpMessageNotWritableException exception) {
            return handleHttpMessageNotWritable(
                    exception,
                    HttpHeaders.EMPTY,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new ServletWebRequest(new MockHttpServletRequest())
            );
        }
    }

    static class TestJacksonException extends JacksonException {
        TestJacksonException(String message) {
            super(message);
        }
    }
}
