package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ChangedFieldByFileFieldContainer extends ChangedFieldContainer {

    private final Map<String, Map<String, Object>> changedFields;

    @Setter
    private boolean isSave;

    public ChangedFieldByFileFieldContainer() {
        this.changedFields = new HashMap<String, Map<String, Object>>();
        this.isSave = false;
    }

    public ChangedFieldByFileFieldContainer(boolean isSave) {
        this.changedFields = new HashMap<String, Map<String, Object>>();
        this.isSave = isSave;
    }

    public ChangedFieldByFileFieldContainer(ChangedFieldContainer container, boolean isSave) {
        this.changedFields = container.getChangedFields();
        this.isSave = isSave;
    }

    public void putAll(Map<String, ChangedField> changed) {
        changed.forEach((key, value) -> changedFields.put(key, value.getAttributes()));
    }
}
