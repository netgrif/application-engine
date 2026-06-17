package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.validation.constraints.NotNull;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Service for managing security context object, like user resource
 */
@Slf4j
@Service
public class SecurityContextServiceImpl implements SecurityContextService {

    /**
     * List containing user IDs that's state was changed during an action
     */
    private final Set<String> cachedTokens;

    private UserService userService;

    private final SecurityContextRepository securityContextRepository;

    protected SecurityContextServiceImpl() {
        this.cachedTokens = ConcurrentHashMap.newKeySet();
        this.securityContextRepository = new HttpSessionSecurityContextRepository();
    }

    /**
     * Saves token to the cache if the state of context object was changed and needs to be updated
     *
     * @param token the token string to be cached
     */
    @Override
    public void saveToken(String token) {
        this.cachedTokens.add(token);
    }

    /**
     * Reloads the security context according to currently logged user
     *
     * @param loggedUser the user whose context needs to be reloaded
     */
    @Override
    public void reloadSecurityContext(LoggedUser loggedUser) {
        reloadSecurityContext(loggedUser, false);
    }

    /**
     * Reloads the security context according to currently logged user from database
     *
     * @param loggedUser the user whose context needs to be reloaded
     */
    @Override
    public void forceReloadSecurityContext(LoggedUser loggedUser) {
        reloadSecurityContext(loggedUser, true);
    }

    private void reloadSecurityContext(LoggedUser loggedUser, boolean forceRefresh) {
        if ((isUserLogged(loggedUser) && cachedTokens.contains(loggedUser.getStringId())) || forceRefresh) {
            if (forceRefresh) {
                AbstractUser user = userService.findUserByUsername(loggedUser.getUsername(), loggedUser.getRealmId()).orElseThrow(() -> new NoSuchElementException("User not found"));
                loggedUser = ActorTransformer.toLoggedUser(user);
            }
            SecurityContext context = getSecurityContext(loggedUser);
            securityContextRepository.saveContext(context, getServletRequest(), getServletResponse());
            clearToken(loggedUser.getStringId());
        }
    }

    @NotNull
    private SecurityContext getSecurityContext(LoggedUser loggedUser) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loggedUser, SecurityContextHolder.getContext().getAuthentication().getCredentials(), ((LoggedUserImpl) loggedUser).getAuthorities());
        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        return context;
    }

    private HttpServletRequest getServletRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    private HttpServletResponse getServletResponse() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
    }

    /**
     * Checks type of SecurityContext
     *
     * @return true if the SecurityContext exists and is of type LoggedUser
     */
    @Override
    public boolean isAuthenticatedPrincipalLoggedUser() {
        return SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof LoggedUser;
    }

    /**
     * Removes token from cache if the state of context object was updated
     *
     * @param token the token string to be removed from cache
     */
    private void clearToken(String token) {
        if (cachedTokens.contains(token))
            this.cachedTokens.remove(token);
    }

    /**
     * Checks whether the user is logged in
     *
     * @param loggedUser the user that is needed to be checked
     * @return true if logged user is in the security context
     */
    private boolean isUserLogged(LoggedUser loggedUser) {
        if (isAuthenticatedPrincipalLoggedUser())
            return ((LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId().equals(loggedUser.getId());
        else
            return false;
    }

    @Autowired
    @Lazy
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
