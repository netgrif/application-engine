package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class DataSet {

    /**
     * Field import ID: dataField
     */
    private LinkedHashMap<String, Field<?>> fields;

    public DataSet() {
        this.fields = new LinkedHashMap<>();
    }

    public Field<?> get(String fieldId) {
        return fields.get(fieldId);
    }

    public Field<?> put(String fieldId, Field<?> dataField) {
        return fields.put(fieldId, dataField);
    }

    public static DataSet of (String fieldId, Field<?> dataField) {
        DataSet dataSet = new DataSet();
        dataSet.put(fieldId, dataField);
        return dataSet;
    }
}
