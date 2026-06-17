package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

/**
 * Implementation of {@link Authority} that also implements Spring Security's {@link GrantedAuthority} interface.
 * This class serves as a bridge between the application's authority model and Spring Security's authorization system.
 * It extends the base {@link Authority} class to provide compatibility with Spring Security authentication mechanisms.
 *
 * @see Authority
 * @see GrantedAuthority
 */
@NoArgsConstructor
public class AuthorityImpl extends Authority implements GrantedAuthority {

    /**
     * Constructs an AuthorityImpl with the specified authority string.
     *
     * @param authority the string representation of the authority to be granted
     */
    public AuthorityImpl(String authority) {
        super(authority);
    }

    /**
     * Constructs an AuthorityImpl from an existing Authority object.
     * This constructor allows conversion from the base Authority type to this Spring Security compatible implementation.
     *
     * @param authority the Authority object to copy authorities from
     */
    public AuthorityImpl(Authority authority) {
        super(authority);
    }
}