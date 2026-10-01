package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import lombok.Data;

@Data
public class AuthorityDto {

    private String id;

    private String name;

    public AuthorityDto(Authority authority) {
        this.id = authority.getStringId();
        this.name = authority.getName();
    }
}
