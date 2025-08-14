package com.netgrif.application.engine.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CaseField extends DataField {

    @Field(type = Text)
    private List<String> caseValue;

    public CaseField(List<String> value) {
        super(value.toString());
        this.caseValue = value;
    }
}
