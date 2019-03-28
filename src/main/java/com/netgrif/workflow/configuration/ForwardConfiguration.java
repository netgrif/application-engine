package com.netgrif.workflow.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@Controller
@ControllerAdvice
public class ForwardConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ForwardConfiguration.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handle404Error() {
        log.info("No requested mapping found. Forwarding to api index");
        return "forward:/api";
    }

}
