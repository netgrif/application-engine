package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.interfaces.ILoginAttemptService;
import com.netgrif.workflow.event.events.user.UserLoginEvent;
import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.orgstructure.domain.Member;
import com.netgrif.workflow.orgstructure.service.IMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ILoginAttemptService loginAttemptService;

    @Autowired
    private HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String ip = getClientIP();
        if (loginAttemptService.isBlocked(ip)) {
            logger.info("User "+email+" with IP Address "+ip+" is blocked.");
            throw new RuntimeException("blocked");
        }

        LoggedUser loggedUser = getLoggedUser(email);
        setGroups(loggedUser);

        publisher.publishEvent(new UserLoginEvent(loggedUser));

        return loggedUser;
    }

    public void reloadSecurityContext(LoggedUser loggedUser){
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loggedUser, SecurityContextHolder.getContext().getAuthentication().getCredentials(), loggedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private LoggedUser getLoggedUser(String email) throws UsernameNotFoundException{
        User user = userRepository.findByEmail(email);
        if(user == null)
            throw new UsernameNotFoundException("No user was found for login: " + email);
        if ( user.getPassword() == null || user.getState() != UserState.ACTIVE)
            throw new UsernameNotFoundException("User with login "+email+" cannot be logged in!");

        return user.transformToLoggedUser();
    }

    private void setGroups(LoggedUser loggedUser) {
        Member member = memberService.findByEmail(loggedUser.getUsername());
        if (member != null) {
            loggedUser.setGroups(member.getGroups().stream().map(Group::getId).collect(Collectors.toSet()));
        }
    }

    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null){
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}