package com.netgrif.application.engine.petrinet.domain.roles;

import com.netgrif.application.engine.utils.UniqueKeyMap;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Permissions<T> {
    /**
     * todo javadoc
     * key is roleId
     * */
    private UniqueKeyMap<String, Map<T, Boolean>> permissions;

    public Permissions() {
        this.permissions = new UniqueKeyMap<>();
    }

    public void addPermission(String roleId, T permission, Boolean value) {

    }

    public void addPermissions(String roleId, Map<T, Boolean> permissions) {

    }

}
