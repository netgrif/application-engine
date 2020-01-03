package com.netgrif.workflow.elastic.domain.mapping;

import com.netgrif.workflow.elastic.domain.ElasticCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public abstract class DataField {

    @Id
    private String id;

    @Version
    private Long version;

    @Field(type = Text)
    public String fulltext;

    private JoinField dataSetJoin;

    public DataField(String fulltextValue) {
        this.fulltext = fulltextValue;
    }

    public void createParentLink(ElasticCase parent) {
        this.dataSetJoin = new JoinField("data", parent.getId());
    }

    public abstract String getIndex();
}