package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authentication.domain.Authority;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "roleAssignment")
@EqualsAndHashCode(callSuper = true)
public class ApplicationRoleAssignment extends RoleAssignment {
    @Indexed
    private String applicationId;

    /**
     * todo javadoc
     */
    public Authority toSessionAuthority() {
        return new Authority(getStringId(), roleId);
    }
}
