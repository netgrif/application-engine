package com.netgrif.application.engine.petrinet.domain.dataset

class StringCollectionField extends Field<List<String>> {

    StringCollectionField() {
        super()
        this.defaultValue = new ArrayList<>()
    }

    @Override
    FieldType getType() {
        return FieldType.STRING_COLLECTION
    }

    @Override
    void clearValue() {
        this.setValue(new ArrayList<String>())
    }

    @Override
    Field clone() {
        StringCollectionField clone = new StringCollectionField()
        super.clone(clone)
        return clone
    }

    void setDefaultValue(List<String> defaultValue) {
        super.setDefaultValue(defaultValue)
    }
}
