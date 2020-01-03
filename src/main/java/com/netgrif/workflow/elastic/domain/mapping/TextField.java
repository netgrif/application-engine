package com.netgrif.workflow.elastic.domain.mapping;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(indexName = "#{@elasticDataIndexPrefix}"+"#{@elasticTextDataIndexSuffix}", type = "data")
public class TextField extends DataField {

    @Field(type = Text)
    public String searchable;

    @Field(type = Keyword)
    public String sortable;

    public TextField(String value) {
        super(value);
        this.searchable = value;
        this.sortable = value;
    }
}