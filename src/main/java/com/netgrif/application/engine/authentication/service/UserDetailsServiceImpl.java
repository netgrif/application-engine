package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.User;
import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.repositories.UserRepository;
import com.netgrif.application.engine.authentication.service.interfaces.ILoginAttemptService;
import com.netgrif.application.engine.event.events.user.UserLoginEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    protected final UserRepository userRepository;

    protected final ApplicationEventPublisher publisher;

    protected final ILoginAttemptService loginAttemptService;

    protected final HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String ip = getClientIP();
        if (loginAttemptService.isBlocked(ip)) {
            log.info("User {} with IP address {} is blocked.", email, ip);
            throw new RuntimeException("blocked");
        }

        Identity identity = getLoggedUser(email);

        publisher.publishEvent(new UserLoginEvent(identity));

        return identity;
    }

    protected Identity getLoggedUser(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null)
            throw new UsernameNotFoundException("No user was found for login: " + email);
        if (user.getPassword() == null || user.getState() != IdentityState.ACTIVE)
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