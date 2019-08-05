package com.netgrif.workflow.elastic.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
public class DataField {

    @Field(type = Keyword)
    public String id;

    public String value;

    @Field(type = Keyword)
    public String sortable;

    public DataField(String id, String value) {
        this(id, value, value);
    }

    public DataField(String id, String value, String sortable) {
        this.id = id;
        this.value = value;
        this.sortable = sortable;
    }
}