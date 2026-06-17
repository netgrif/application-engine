package com.netgrif.application.engine.adapter.spring.petrinet.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UriNode extends com.netgrif.application.engine.objects.petrinet.domain.UriNode {

    public UriNode() {
        super();
    }

    public UriNode(com.netgrif.application.engine.objects.petrinet.domain.UriNode uriNode) {
        super(uriNode);
    }

    @Id
    @Override
    public String getPath() {
        return super.getPath();
    }
}
