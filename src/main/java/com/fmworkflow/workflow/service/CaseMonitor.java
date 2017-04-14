package com.fmworkflow.workflow.service;

import com.fmworkflow.workflow.domain.Case;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CaseMonitor {
    @AfterReturning(
            pointcut = "execution(* com.fmworkflow.workflow.domain.CaseRepository.findOne(..))",
            returning= "result")
    public void afterFindOne(JoinPoint joinPoint, Object result) {
        Case useCase = (Case) result;
        useCase.getPetriNet().initializeArcs();
        useCase.getPetriNet().initializeTokens(useCase.getActivePlaces());
    }
}
