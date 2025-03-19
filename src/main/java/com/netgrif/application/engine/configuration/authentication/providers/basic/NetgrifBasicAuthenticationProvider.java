package com.netgrif.application.engine.configuration.authentication.providers.basic;


import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.configuration.authentication.providers.NetgrifAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NetgrifBasicAuthenticationProvider extends NetgrifAuthenticationProvider {

    protected final IIdentityService identityService;

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

        Optional<Identity> identityOpt = identityService.findByUsername(authentication.getName());
        if (identityOpt.isEmpty()) {
            log.debug("Identity not found by name");
            loginAttemptService.loginFailed(key);
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        Identity identity = identityOpt.get();
        String presentedPassword = authentication.getCredentials().toString();
        if (!this.passwordEncoder.matches(presentedPassword, identity.getPassword())) {
            log.debug("Failed to authenticate since password does not match stored value");
            loginAttemptService.loginFailed(key);
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(identity.toSession(),
                presentedPassword, new HashSet<>());
        result.setDetails(authentication.getDetails());
        loginAttemptService.loginSucceeded(key);
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
