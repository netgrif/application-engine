package com.netgrif.application.engine.petrinet.domain.dataset

class UserListField extends Field<UserListFieldValue> {

    UserListField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.USERLIST;
    }

    @Override
    Field clone() {
        UserListField clone = new UserListField()
        super.clone(clone)
        return clone
    }
}
