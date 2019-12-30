package com.netgrif.workflow.elastic.domain.mapping;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(indexName = "#{@elasticCaseIndex}", type = "data_number")
public class NumberField extends DataField {

    @Field(type = FieldType.Double)
    public Double searchable;

    @Field(type = FieldType.Double)
    public Double sortable;

    public NumberField(Double value) {
        super(value.toString());
        this.searchable = value;
        this.sortable = value;
    }
}