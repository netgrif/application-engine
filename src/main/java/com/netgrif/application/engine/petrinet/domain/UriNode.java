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
    private String uriPath;

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
    @Field(type = FieldType.Integer)
    private int level;

    @Getter
    @Setter
    private Set<UriContentType> contentTypes;

    public UriNode() {
        this.childrenId = new HashSet<>();
        this.children = new HashSet<>();
        this.contentTypes = new HashSet<>();
    }

    public boolean containsCase() {
        return contentTypes.contains(UriContentType.CASE);
    }

    public void addContentType(UriContentType contentType) {
        if (contentTypes == null)
            contentTypes = new HashSet<>();
        contentTypes.add(contentType);
    }

    public boolean containsNet() {
        return contentTypes.contains(UriContentType.PROCESS);
    }
}
