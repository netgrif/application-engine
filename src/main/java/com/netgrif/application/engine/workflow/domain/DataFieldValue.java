package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.netgrif.application.engine.petrinet.converter.DataFieldValueDeserializer;
import com.netgrif.application.engine.petrinet.converter.DataFieldValueSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonSerialize(using = DataFieldValueSerializer.class)
@JsonDeserialize(using= DataFieldValueDeserializer.class)
public class DataFieldValue<T> {

    private T value;

    @Override
    public String toString() {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}
