package com.netgrif.workflow.elastic.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
public class DataField {

    public String value;

    @Field(type = Keyword)
    public String sortable;

    public DataField(String value) {
        this.value = value;
        this.sortable = value;
    }

    public DataField(String value, String sortable) {
        this.value = value;
        this.sortable = sortable;
    }
}