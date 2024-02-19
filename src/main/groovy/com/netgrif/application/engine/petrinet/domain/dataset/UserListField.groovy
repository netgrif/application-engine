package com.netgrif.application.engine.petrinet.domain.dataset

class UserListField extends Field<UserListFieldValue> {

    Set<String> roles

    UserListField() {
        super()
        this.roles = new HashSet<>()
    }

    UserListField(String[] values) {
        this()
        if (values != null) {
            this.roles.addAll(Arrays.asList(values))
        }
    }

    Set<String> getRoles() {
        return roles
    }

    @Override
    FieldType getType() {
        return FieldType.USERLIST;
    }

    @Override
    Field clone() {
        UserListField clone = new UserListField()
        super.clone(clone)
        clone.roles = this.roles
        return clone
    }
}
