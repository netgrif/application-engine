package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@NoArgsConstructor
public class AuthorityImpl extends Authority implements GrantedAuthority {

    public AuthorityImpl(String authority) {
        super(authority);
    }

    public AuthorityImpl(Authority authority) {
        super(authority);
    }
}
