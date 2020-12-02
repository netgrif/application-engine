package com.netgrif.workflow.petrinet.domain.dataset;

import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.petrinet.domain.roles.RolePermission
import org.elasticsearch.client.Node;

import java.util.HashSet;

class UserListField extends Field<HashSet<User>> {


    Map<String, HashSet<RolePermission>> permissions

    UserListField(){
        super()
        this.permissions = new HashMap<>()
    }

    UserListField(Map<String, HashSet<RolePermission>> permissions){
        this()
        this.permissions = permissions
    }

    Map<String, HashSet<RolePermission>> getPermissions() {
        return permissions
    }

    void setPermissions(Map<String, HashSet<RolePermission>> permissions) {
        this.permissions = permissions
    }

    @Override
    FieldType getType() {
        return FieldType.USERLIST;
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(null)
    }

    @Override
    Field clone() {
        UserField clone = new UserField()
        super.clone(clone)
        return clone
    }
}
