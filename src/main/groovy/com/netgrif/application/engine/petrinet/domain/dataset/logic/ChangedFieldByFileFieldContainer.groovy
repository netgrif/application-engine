package com.netgrif.application.engine.petrinet.domain.dataset.logic

class ChangedFieldByFileFieldContainer extends ChangedFieldContainer {

    private Map<String, Map<String, Object>> changedFields
    private boolean isSave

    ChangedFieldByFileFieldContainer() {
        this.changedFields = new HashMap<>()
        this.isSave = false
    }

    ChangedFieldByFileFieldContainer(boolean isSave) {
        this.changedFields = new HashMap<>()
        this.isSave = isSave
    }

    ChangedFieldByFileFieldContainer(ChangedFieldContainer container, boolean isSave) {
        this.changedFields = container.getChangedFields()
        this.isSave = isSave
    }

    void putAll(Map<String, ChangedField> changed) {
        changed.each { key, field -> changedFields.put(field.id, field.attributes) }
    }

    Map<String, Map<String, Object>> getChangedFields() {
        return changedFields
    }

    boolean getIsSave() {
        return isSave
    }

    void setIsSave(boolean isSave) {
        this.isSave = isSave
    }
}
