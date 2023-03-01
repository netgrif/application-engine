package com.netgrif.application.engine.configuration.authentication.providers.basic;


import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.auth.domain.repositories.UserRepository;
import com.netgrif.application.engine.configuration.authentication.providers.NetgrifAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
public class NetgrifBasicAuthenticationProvider extends NetgrifAuthenticationProvider {

    @Autowired
    protected UserRepository userRepository;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    protected PasswordEncoder passwordEncoder;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
        String key = details.getRemoteAddress();
        if (key == null) {
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        if (loginAttemptService.isBlocked(key)) {
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        String name = authentication.getName();
        User user = userRepository.findByEmail(name);
        if (user == null) {
            log.debug("User not found");
            loginAttemptService.loginFailed(key);
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        String presentedPassword = authentication.getCredentials().toString();
        if (!this.passwordEncoder.matches(presentedPassword, user.getPassword())) {
            log.debug("Failed to authenticate since password does not match stored value");
            loginAttemptService.loginFailed(key);
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        UserDetails userDetails = user.transformToLoggedUser();

        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(userDetails, presentedPassword, userDetails.getAuthorities());
        result.setDetails(authentication.getDetails());
        loginAttemptService.loginSucceeded(user.getStringId());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }


    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
        this.passwordEncoder = passwordEncoder;
    }

}
