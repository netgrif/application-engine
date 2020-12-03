package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.auth.domain.User;

class UserListField extends Field<List<Long>> {

    UserListField(){
        super()
    }

    @Override
    void setValue(List<User> value) {
        super.setValue(value)
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
        UserListField clone = new UserListField()
        super.clone(clone)
        return clone
    }
}
