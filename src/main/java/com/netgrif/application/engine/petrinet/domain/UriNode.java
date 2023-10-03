package com.netgrif.application.engine.petrinet.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.HashSet;
import java.util.Set;


@AllArgsConstructor
public class UriNode {

    @Id
    @Getter
    private ObjectId _id;

    @Getter
    @Setter
    private String uriPath;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String parentId;

    @Getter
    @Setter
    @Transient
    private UriNode parent;

    @Getter
    @Setter
    private Set<String> childrenId;

    @Getter
    @Setter
    @Transient
    private Set<UriNode> children;

    @Getter
    @Setter
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
        if (contentTypes == null) {
            contentTypes = new HashSet<>();
        }
        contentTypes.add(contentType);
    }

    public boolean containsNet() {
        return contentTypes.contains(UriContentType.PROCESS);
    }

    public String getStringId() {
        return this._id.toString();
    }
}
