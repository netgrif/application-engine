package com.netgrif.application.engine.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@ControllerAdvice
public class ForwardConfiguration {

    @RequestMapping(value = "/{path:[^api]*}")
    public String redirect(HttpServletRequest request) {
        log.info("Forwarding to root for request URI [" + request.getRequestURI() + "]");
        return "forward:/";
    }

    @RequestMapping(value = {"/signup/{token}", "/recover/{token}"})
    public String redirectWithToken(@PathVariable("token") String token, HttpServletRequest request) {
        log.info("Forwarding to root for URI [ " + request.getRequestURI() + " ] with token " + token);
        return "forward:/";
    }


    @RequestMapping("/**/{path:[^.]+}")
    public String forward() {
        return "forward:/";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handle404Error() {
        log.info("No requested mapping found. Forwarding to index");
        return "forward:/";
    }
}
