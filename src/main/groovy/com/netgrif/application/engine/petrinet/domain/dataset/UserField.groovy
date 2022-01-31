package com.netgrif.application.engine.petrinet.domain.dataset


import org.springframework.data.mongodb.core.mapping.Document

@Document
class UserField extends Field<UserFieldValue> {

    Set<String> roles

    UserField() {
        super()
        this.roles = new HashSet<>()
    }

    UserField(String[] values) {
        this()
        if (values != null) {
            this.roles.addAll(Arrays.asList(values))
        }
    }

    @Override
    FieldType getType() {
        return FieldType.USER
    }

    Set<String> getRoles() {
        return roles
    }

    @Override
    Field clone() {
        UserField clone = new UserField()
        super.clone(clone)
        clone.roles = this.roles
        return clone
    }
}