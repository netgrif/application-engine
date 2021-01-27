package com.netgrif.workflow.petrinet.domain.dataset

class UserListField extends Field<List<Long>> {

    UserListField(){
        super()
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
