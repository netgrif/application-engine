package com.netgrif.application.engine.petrinet.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.HashSet;
import java.util.Set;
import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@AllArgsConstructor
@Document(indexName = "#{@elasticUriIndex}")
public class UriNode {

    @Id
    @Getter
    private String id;

    @Getter
    @Setter
    @Field(type = Keyword)
    private String uri;

    @Getter
    @Setter
    @Field(type = Keyword)
    private String name;

    @Getter
    @Setter
    @Field(type = Keyword)
    private String parentId;

    @Getter
    @Setter
    @Transient
    private UriNode parent;

    @Getter
    @Setter
    @Field(type = Keyword)
    private Set<String> childrenId;

    @Getter
    @Setter
    @Transient
    private Set<UriNode> children;

    @Getter
    @Setter
    @Field(type = FieldType.Boolean)
    private boolean root;

    @Getter
    @Setter
    @Field(type = FieldType.Boolean)
    private boolean containsCase;

    @Getter
    @Setter
    @Field(type = FieldType.Boolean)
    private boolean containsProcess;

    public UriNode() {
        this.childrenId = new HashSet<>();
        this.children = new HashSet<>();
    }

    public boolean containsCase() {
        return containsCase;
    }

    public void setContainsCase(boolean containsCase) {
        this.containsCase = containsCase;
    }

    public boolean containsNet() {
        return containsProcess;
    }

    public void setContainsProcess(boolean containsProcess) {
        this.containsProcess = containsProcess;
    }
}
