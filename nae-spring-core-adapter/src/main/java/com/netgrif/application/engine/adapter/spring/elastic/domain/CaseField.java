package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@NoArgsConstructor
public class CaseField extends com.netgrif.application.engine.objects.elastic.domain.CaseField {

    public CaseField(CaseField field) {
        super(field);
    }

    public CaseField(String[] values, String[] allowedNets) {
        super(values, allowedNets);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = Text)
    public String[] getAllowedNets() {
        return super.allowedNets;
    }

    @Override
    @Field(type = Text)
    public List<String> getCaseValue() {
        return super.getCaseValue();
    }
}
