package com.netgrif.workflow.petrinet.domain.dataset

class UserListField extends Field<List<Long>> {

    UserListField(){
        super()
        this.defaultValue = new ArrayList<>()
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
