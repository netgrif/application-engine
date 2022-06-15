package com.netgrif.application.engine.navtree.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.util.Set;
import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@elasticNavIndex}")
public class NavNode {

    @Id
    private String id;

    @Field(type = Keyword)
    private String name;

    @Field(type = Keyword)
    private String parent;

    @Field(type = Keyword)
    private Set<String> children;

    @Field(type = FieldType.Boolean)
    private boolean root;

    @Field(type = FieldType.Boolean)
    private boolean containsCase;

    @Field(type = FieldType.Boolean)
    private boolean containsNet;

    public boolean containsCase() {
        return containsCase;
    }

    public void setContainsCase(boolean containsCase) {
        this.containsCase = containsCase;
    }

    public boolean containsNet() {
        return containsNet;
    }

    public void setContainsNet(boolean containsNet) {
        this.containsNet = containsNet;
    }
}
