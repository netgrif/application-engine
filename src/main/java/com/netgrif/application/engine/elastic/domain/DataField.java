package com.netgrif.application.engine.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class DataField {

    @Field(type = Text)
    public List<String> fulltextValue = new ArrayList<>();

    DataField(String fulltextValue) {
        this.fulltextValue = List.of(fulltextValue);
    }
}