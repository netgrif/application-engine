package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangedFieldContainer implements Serializable {

    @Serial
    private static final long serialVersionUID = 2299918326411121185L;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Map<String, Object>> changedFields;

    public ChangedFieldContainer() {
        changedFields = new HashMap<>();
    }

    public void putAll(Map<String, ChangedField> changed) {
        changed.forEach((key, value) -> changedFields.put(key, value.getAttributes()));
    }
}
