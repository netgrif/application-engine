package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class PublicAbstractController {

    @Value("${anonymous.email.address}")
    private String anonymousEmail;

    protected final IUserService userService;


    public PublicAbstractController(IUserService userService) {
        this.userService = userService;
    }

    protected LoggedUser getAnonym() {
        /*LoggedUser user = userService.findByEmail(anonymousEmail, true).transformToLoggedUser();
        reloadSecurityContext(user);*/
        return (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /*protected void reloadSecurityContext(LoggedUser loggedUser) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loggedUser, SecurityContextHolder.getContext().getAuthentication().getCredentials(), loggedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }*/
}
