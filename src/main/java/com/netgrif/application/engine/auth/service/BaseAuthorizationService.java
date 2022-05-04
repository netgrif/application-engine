package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.Authorize;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.configuration.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Service
public class BaseAuthorizationService extends AbstractBaseAuthorizationService {

    public BaseAuthorizationService(@Autowired IUserService userService) {
        super(userService);
    }

    @Pointcut(value = "@annotation(authorize))")
    private void authorizingMethod(Authorize authorize) {}

    @Around(value = "authorizingMethod(authorize)", argNames = "joinPoint,authorize")
    public Object authorize(ProceedingJoinPoint joinPoint, Authorize authorize) throws Throwable {
        if (isAllowedByExpression(joinPoint, authorize.expression()) || hasAuthority(authorize.authority())) {
            return joinPoint.proceed();
        } else {
            return null;
        }
    }

    private boolean isAllowedByExpression(ProceedingJoinPoint joinPoint, String expression) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(ApplicationContextProvider.getAppContext()));

        List<Object> args = Arrays.asList(joinPoint.getArgs());
        List<String> argNames = Arrays.asList(((MethodSignature) joinPoint.getSignature()).getParameterNames());

        argNames.forEach(name -> context.setVariable(name, args.get(argNames.indexOf(name))));

        boolean allowed;
        try {
            allowed = ExpressionUtils.evaluateAsBoolean(parser.parseExpression(expression), context);
            return allowed;
        } catch (EvaluationException | NullPointerException e) {
            log.warn("Failed to parse expression '" + expression + "'.");
            return false;
        }
    }
}
