package com.netgrif.workflow.history.domain;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.workflow.domain.Case;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "eventLog")
public class CaseSaveDataEventLog extends CaseEventLog {

    @Field("changedFields")
    private Map<String, Map<String, Object>> data;

    public CaseSaveDataEventLog(Case useCase, Collection<ChangedField> changedFields, Map<String, String> userData) {
        super(useCase);
        this.setDataSetValues(null);
        this.data = changedFields.stream().collect(Collectors.toMap(ChangedField::getId, ChangedField::getAttributes));
        userData.forEach((s, s2) -> {
            Map<String, Object> change = new HashMap<>();
            change.put("value", s2);
            data.put(s, change);
        });
    }
}