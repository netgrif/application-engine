package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.Authorize;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Slf4j
@Aspect
@Service
public class BaseAuthorizationService extends AbstractBaseAuthorizationService {

    public BaseAuthorizationService(@Autowired IUserService userService) {
        super(userService);
    }

    @Around("@annotation(authorize)")
    @PreAuthorize("#authorize.preAuthorize()")
    public Object authorize(ProceedingJoinPoint joinPoint, Authorize authorize) throws Throwable {
        if (hasAuthority(authorize.authority())) {
            return joinPoint.proceed();
        } else {
            return null;
        }
    }
}
