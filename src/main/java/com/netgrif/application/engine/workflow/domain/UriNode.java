package com.netgrif.application.engine.workflow.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;


@Getter
@Document
@AllArgsConstructor
public class UriNode {

    @Id
    private ObjectId id;

    @Setter
    private String uriPath;

    @Setter
    private String name;

    @Setter
    private String parentId;

    @Setter
    @Transient
    private UriNode parent;

    @Setter
    private Set<String> childrenId;

    @Setter
    @Transient
    private Set<UriNode> children;

    @Setter
    private int level;

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
        if (contentTypes == null) {
            contentTypes = new HashSet<>();
        }
        contentTypes.add(contentType);
    }

    public boolean containsNet() {
        return contentTypes.contains(UriContentType.PROCESS);
    }

    public String getStringId() {
        return this.id.toString();
    }
}
