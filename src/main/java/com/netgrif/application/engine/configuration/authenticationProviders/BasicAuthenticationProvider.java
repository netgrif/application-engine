package com.netgrif.application.engine.configuration.authenticationProviders;


import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.auth.domain.UserState;
import com.netgrif.application.engine.auth.domain.repositories.UserRepository;
import com.netgrif.application.engine.event.events.user.UserLoginEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;

@Slf4j
@Component
public class BasicAuthenticationProvider extends NetgrifAuthenticationProvider {

    @Autowired
    protected UserRepository userRepository;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    protected PasswordEncoder passwordEncoder;

    protected String mfa;



    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        User user = userRepository.findByEmail(name);
        if (user == null) {
            log.debug("User not found");
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        String presentedPassword = authentication.getCredentials().toString();
        if (!this.passwordEncoder.matches(presentedPassword, user.getPassword())) {
            log.debug("Failed to authenticate since password does not match stored value");
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        if (mfa != null) {
            System.out.println(mfa);
        }


        UserDetails userDetails = user.transformToLoggedUser();
        return new UsernamePasswordAuthenticationToken(userDetails, presentedPassword, new ArrayList<>());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    protected String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @Override
    @Primary
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String ip = getClientIP();
        if (loginAttemptService.isBlocked(ip)) {
            log.info("User " + email + " with IP Address " + ip + " is blocked.");
            throw new RuntimeException("blocked");
        }

        LoggedUser loggedUser = getLoggedUser(email);
        loggedUser.setFullName("JOZIKEEEEE");
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


    /**
     * Sets the PasswordEncoder instance to be used to encode and validate passwords. If
     * not set, the password will be compared using
     * {@link PasswordEncoderFactories#createDelegatingPasswordEncoder()}
     *
     * @param passwordEncoder must be an instance of one of the {@code PasswordEncoder}
     *                        types.
     */
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
        this.passwordEncoder = passwordEncoder;
    }

    public void setMFA(String totok) {
        System.out.println("BLA BLA BLA BLA");
        this.mfa = totok;
    }

}
