package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.auth.domain.UserState;
import com.netgrif.application.engine.auth.domain.repositories.UserRepository;
import com.netgrif.application.engine.auth.service.interfaces.ILoginAttemptService;
import com.netgrif.application.engine.event.events.user.UserLoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    protected UserRepository userRepository;

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

        LoggedUser loggedUser = getLoggedUser(email);

        publisher.publishEvent(new UserLoginEvent(loggedUser));

        return loggedUser;
    }

    protected LoggedUser getLoggedUser(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null)
            throw new UsernameNotFoundException("No user was found for login: " + email);
        if (user.getPassword() == null || user.getState() != UserState.ACTIVE)
            throw new UsernameNotFoundException("User with login " + email + " cannot be logged in!");

        return user.transformToLoggedUser();
    }


    protected String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}