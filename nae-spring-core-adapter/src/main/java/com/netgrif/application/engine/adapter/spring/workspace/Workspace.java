package com.netgrif.application.engine.adapter.spring.workspace;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Workspace extends com.netgrif.application.engine.objects.workspace.Workspace {

    // todo 2072 indexable

    public Workspace(String id) {
        super(id);
    }

    @Id
    @Override
    public String getId() {
        return super.getId();
    }
}
