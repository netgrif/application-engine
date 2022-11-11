package com.netgrif.application.engine.configuration.authentication.providers.basic;

import com.netgrif.application.engine.configuration.authentication.providers.NetgrifAuthenticationProvider;
import com.netgrif.application.engine.configuration.authentication.tokens.NetgrifAnonymousAuthenticationToken;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.util.Assert;

public class NetgrifAnonymousAuthenticationProvider  extends NetgrifAuthenticationProvider {

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private final String key;

    public NetgrifAnonymousAuthenticationProvider(String key) {
        Assert.hasLength(key, "A Key is required");
        this.key = key;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (!supports(authentication.getClass())) {
            return null;
        }
        if (this.key.hashCode() != ((NetgrifAnonymousAuthenticationToken) authentication).getKeyHash()) {
            throw new BadCredentialsException(this.messages.getMessage("AnonymousAuthenticationProvider.incorrectKey",
                    "The presented AnonymousAuthenticationToken does not contain the expected key"));
        }
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(NetgrifAnonymousAuthenticationToken.class);
    }
}
