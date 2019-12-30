package com.netgrif.workflow.elastic.domain.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Document(indexName = "#{@elasticCaseIndex}", type = "data_fulltext")
public class DataField {

    @Id
    private String id;

    @Field(type = Text)
    public String fulltext;

    public DataField(String fulltext) {
        this.fulltext = fulltext;
    }
}