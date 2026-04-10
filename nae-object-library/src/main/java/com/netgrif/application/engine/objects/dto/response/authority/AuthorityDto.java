package com.netgrif.application.engine.objects.dto.response.authority;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * DTO for {@link Authority}
 */
public record AuthorityDto(ObjectId id, String name) implements Serializable {

    public static AuthorityDto fromAuthority(Authority authority) {
        return new AuthorityDto(authority.get_id(), authority.getName());
    }

}