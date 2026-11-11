package com.netgrif.application.engine.auth.service;


import com.netgrif.application.engine.adapter.spring.utils.NaeReflectionUtils;
import com.netgrif.application.engine.auth.context.CallOriginResolver;
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
 * Aspect service providing method-level authorization checks via {@link Authorize} and {@link Authorizations} annotations.
 * It intercepts annotated method executions, evaluates SpEL expressions within the execution context,
 * and verifies authority requirements against the currently authenticated user.
 */
@Slf4j
@Aspect
@Service
public class BaseAuthorizationServiceAspect {

    private final UserService userService;

    private final ExpressionParser parser;

    private final BeanFactoryResolver beanResolver;

    private final CallOriginResolver callOriginResolver;

    /**
     * Creates a new instance of {@link BaseAuthorizationServiceAspect}.
     *
     * @param applicationContext the Spring application context used to resolve beans in SpEL expressions
     * @param userService        the service used to retrieve the currently authenticated user
     * @param callOriginResolver resolver to determine if the call originates from a web request
     */
    public BaseAuthorizationServiceAspect(ApplicationContext applicationContext,
                                          UserService userService,
                                          CallOriginResolver callOriginResolver) {
        this.userService = userService;
        parser = new SpelExpressionParser();
        beanResolver = new BeanFactoryResolver(applicationContext);
        this.callOriginResolver = callOriginResolver;
    }

    /**
     * Advice that handles incoming authorization requests from join points annotated with {@link Authorizations}.
     * Authorization succeeds if at least one of the contained {@link Authorize} annotations evaluates to true.
     *
     * @param joinPoint      the incoming method invocation join point
     * @param authorizations the annotation containing multiple authorization rules
     * @return the result of the method execution
     * @throws Throwable            if the underlying method throws an exception
     * @throws AccessDeniedException if the user does not satisfy the authorization rules
     */
    @Around(value = "@annotation(authorizations)", argNames = "joinPoint,authorizations")
    protected final Object authorize(ProceedingJoinPoint joinPoint, Authorizations authorizations) throws Throwable {
        if (!callOriginResolver.isRequest()) {
            return joinPoint.proceed();
        }
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
     * Advice that handles incoming authorization requests from join points annotated with {@link Authorize}.
     *
     * @param joinPoint the incoming method invocation join point
     * @param authorize the authorization rule annotation
     * @return the result of the method execution
     * @throws Throwable            if the underlying method throws an exception
     * @throws AccessDeniedException if the user does not satisfy the authorization rule
     */
    @Around(value = "@annotation(authorize)", argNames = "joinPoint,authorize")
    protected final Object authorize(ProceedingJoinPoint joinPoint, Authorize authorize) throws Throwable {
        if (!callOriginResolver.isRequest()) {
            return joinPoint.proceed();
        }
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
     * Checks whether the currently logged-in user possesses all specified authorities.
     *
     * @param authorizingObject array of required authorities
     * @return {@code true} if the user has all specified authorities or if the array is empty/null, {@code false} otherwise
     */
    public final boolean hasAnyAuthority(String[] authorizingObject) {
        if (authorizingObject == null || authorizingObject.length == 0 || Arrays.stream(authorizingObject).allMatch(String::isEmpty))
            return true;
        Set<String> loggedUserAuthorities = this.userService.getLoggedUser().getAuthoritySet().stream().map(Authority::getAuthority).collect(Collectors.toSet());
        return loggedUserAuthorities.containsAll(Arrays.asList(authorizingObject));
    }

    /**
     * Parses and evaluates a SpEL expression within the context of the invoked method.
     * Resolves method arguments and registers them as variables in the evaluation context alongside Spring beans.
     *
     * @param joinPoint  the incoming method invocation join point
     * @param expression the SpEL expression to evaluate
     * @return {@code true} if the expression evaluates to true or is empty/null, {@code false} otherwise
     */
    protected boolean isAllowedByExpression(ProceedingJoinPoint joinPoint, String expression) {
        if (expression == null || expression.isEmpty()) {
            return true;
        }
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setBeanResolver(beanResolver);
        List<Object> args = Arrays.asList(joinPoint.getArgs());

        if (NaeReflectionUtils.isGroovyClass(joinPoint.getTarget().getClass())) {
            for (int i = 0; i < args.size(); i++) {
                evaluationContext.setVariable("arg" + i, args.get(i));
            }
        } else {
            List<String> argNames = Arrays.asList(((MethodSignature) joinPoint.getSignature()).getParameterNames());
            argNames.forEach(name -> evaluationContext.setVariable(name, args.get(argNames.indexOf(name))));
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
