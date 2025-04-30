package com.netgrif.application.engine.objects.petrinet.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Setter
@Getter
@AllArgsConstructor
public abstract class UriNode {

//    @Id
    private String path;

    private String name;

    private String parentId;

    //    @Transient
    private UriNode parent;

    private Set<String> childrenId;

    //    @Transient
    private Set<UriNode> children;

    private int level;

    private Set<UriContentType> contentTypes;

    public UriNode() {
        this.childrenId = new HashSet<>();
        this.children = new HashSet<>();
        this.contentTypes = new HashSet<>();
    }

    public UriNode(UriNode uriNode) {
        this.path = uriNode.getPath();
        this.name = uriNode.getName();
        this.parentId = uriNode.getParentId();
        this.parent = uriNode.getParent();
        this.childrenId = uriNode.getChildrenId();
        this.children = uriNode.getChildren();
        this.level = uriNode.getLevel();
        this.contentTypes = uriNode.getContentTypes();
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
        return this.path;
    }
}
