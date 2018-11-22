package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.web.responsebodies.UserResource
import org.springframework.data.mongodb.core.mapping.Document

@Document
class UserField extends Field<User> {

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

    @Override
    void clearValue() {
        super.clearValue()
        setValue(null)
    }
//TODO 20.6.2017 - call when get data fields values
    UserResource toResource() {
        if (this.value == null) return null
        return new UserResource(this.value, "small", true)
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