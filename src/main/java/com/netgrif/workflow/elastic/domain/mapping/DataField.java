package com.netgrif.workflow.elastic.domain.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class DataField {

    @Field(type = Text)
    public String fulltextValue;
}