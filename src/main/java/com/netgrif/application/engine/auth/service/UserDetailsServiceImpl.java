package com.netgrif.application.engine.auth.service;

import com.netgrif.adapter.auth.domain.LoggedUserImpl;
import com.netgrif.adapter.auth.service.UserService;
import com.netgrif.core.auth.domain.IUser;
import com.netgrif.core.auth.domain.LoggedUser;
import com.netgrif.core.auth.domain.User;
import com.netgrif.core.auth.domain.enums.UserState;
import com.netgrif.application.engine.auth.service.interfaces.ILoginAttemptService;
import com.netgrif.core.event.events.user.UserLoginEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    protected UserService userService;

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired
    protected ILoginAttemptService loginAttemptService;

    @Autowired
    protected HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String ip = getClientIP();
        if (loginAttemptService.isBlocked(ip)) {
            logger.info("User " + email + " with IP Address " + ip + " is blocked.");
            throw new RuntimeException("blocked");
        }

        LoggedUserImpl loggedUser = getLoggedUser(email);

        publisher.publishEvent(new UserLoginEvent(loggedUser));

        return loggedUser;
    }

    protected LoggedUserImpl getLoggedUser(String email) throws UsernameNotFoundException {
        IUser user = userService.findByEmail(email, null);
        if (user == null)
            throw new UsernameNotFoundException("No user was found for login: " + email);
        if (((User) user).getPassword() == null || user.getState() != UserState.ACTIVE)
            throw new UsernameNotFoundException("User with login " + email + " cannot be logged in!");

        return (LoggedUserImpl) userService.transformToLoggedUser(user);
    }


    protected String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}