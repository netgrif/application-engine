package com.netgrif.workflow.petrinet.domain.dataset

class UserListField extends FieldWithDefault<List<Long>> {

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
        clone.defaultValue = this.defaultValue
        clone.initExpression = this.initExpression
        return clone
    }
}
