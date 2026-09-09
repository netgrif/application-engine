package com.netgrif.application.engine.auth.service;


import com.netgrif.application.engine.objects.annotations.Authorizations;
import com.netgrif.application.engine.objects.annotations.Authorize;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * The aspect service for authorization system.
 * */
@Slf4j
@Aspect
@Service
public class BaseAuthorizationServiceAspect {

    private final UserService userService;

    private final ExpressionParser parser;

    private final StandardEvaluationContext evaluationContext;

    public BaseAuthorizationServiceAspect(ApplicationContext applicationContext,
                                          UserService userService) {
        this.userService = userService;
        parser = new SpelExpressionParser();
        evaluationContext = new StandardEvaluationContext();
        evaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
    }

    /**
     * The advice that handles incoming authorization requests from join points defined with @annotation(authorization)
     * @param joinPoint the incoming method invocation join point
     * @param authorizations the incoming annotation with authorization parameters
     * */
    @Around(value = "@annotation(authorizations)", argNames = "joinPoint,authorizations")
    protected final Object authorize(ProceedingJoinPoint joinPoint, Authorizations authorizations) throws Throwable {
        boolean result = false;
        if (authorizations.value() != null) {
            for (Authorize authorize : authorizations.value()) {
                result = result || (isAllowedByExpression(joinPoint, authorize.expression()) && hasAnyAuthority(authorize.authority()));
            }
        }
        if (result) {
            return joinPoint.proceed();
        } else {
            throw new AccessDeniedException("Access Denied. User does not have required authorization level.");
        }
    }

    /**
     * The advice that handles incoming authorization requests from join points defined with @annotation(authorization)
     * @param joinPoint the incoming method invocation join point
     * @param authorize the incoming annotation with authorization parameter
     * */
    @Around(value = "@annotation(authorize)", argNames = "joinPoint,authorize")
    protected final Object authorize(ProceedingJoinPoint joinPoint, Authorize authorize) throws Throwable {
        boolean result = false;
        if (authorize != null) {
            result = isAllowedByExpression(joinPoint, authorize.expression()) && hasAnyAuthority(authorize.authority());
        }
        if (result) {
            return joinPoint.proceed();
        } else {
            throw new AccessDeniedException("Access Denied. User does not have required authorization level.");
        }
    }

    /**
     * Checks whether the currently logged user has all the input authorizing objects.
     * @param authorizingObject to be checked for user
     * @return boolean if user has all the authorizing objects.
     * */
    public final boolean hasAnyAuthority(String[] authorizingObject) {
        if (authorizingObject == null || authorizingObject.length == 0 || Arrays.stream(authorizingObject).allMatch(String::isEmpty))
            return true;
        Set<String> loggedUserAuthorities = this.userService.getLoggedUser().getAuthoritySet().stream().map(Authority::getAuthority).collect(Collectors.toSet());
        return loggedUserAuthorities.containsAll(Arrays.asList(authorizingObject));
    }

    /**
     * Parser and evaluator function for Spring-EL expression. It creates parser for SpEL expression, context for it,
     * and evaluates the authorization SpEL expression. It resolves the arguments of invoked method and sets as
     * variables into the evaluation context, because  in the SpEL expression there can be method arguments as variables
     * alongside beans.
     * @param joinPoint the incoming method invocation join point
     * @param expression the SpEL expression
     * @return the evaluated value, whether the SpEL expression returns true or not
     * */
    protected boolean isAllowedByExpression(ProceedingJoinPoint joinPoint, String expression) {
        if (expression == null || expression.isEmpty()) {
            return true;
        }

        List<Object> args = Arrays.asList(joinPoint.getArgs());

        for (int i = 0; i < args.size(); i++) {
            evaluationContext.setVariable("arg" + i, args.get(i));
        }

        boolean allowed;
        try {
            allowed = ExpressionUtils.evaluateAsBoolean(parser.parseExpression(expression), evaluationContext);
            return allowed;
        } catch (EvaluationException | NullPointerException e) {
            log.warn("Failed to parse expression '{}'.", expression);
            return false;
        }
    }
}
