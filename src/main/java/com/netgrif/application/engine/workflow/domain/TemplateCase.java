package com.netgrif.application.engine.workflow.domain;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TemplateCase extends Case {

    /**
     * todo javadoc
     *
     * @param scope
     */
    public TemplateCase(Scope scope) {
        super(scope);
    }
}
