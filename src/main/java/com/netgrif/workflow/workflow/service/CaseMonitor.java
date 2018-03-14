package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.workflow.domain.Case;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CaseMonitor {
    @AfterReturning(
            pointcut = "execution(* com.netgrif.workflow.workflow.domain.repositories.CaseRepository.findOne(..))",
            returning= "result")
    public void afterFindOne(JoinPoint joinPoint, Object result) {
        if (result == null)
            return;
        Case useCase = (Case) result;
        useCase.getPetriNet().initializeArcs();
        useCase.getPetriNet().initializeTokens(useCase.getActivePlaces());
    }
}