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
    Field clone() {
        UserListField clone = new UserListField()
        super.clone(clone)
        return clone
    }
}
