package com.netgrif.application.engine.workflow.domain;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ScopedCase extends Case {

    /**
     * todo javadoc
     *
     * @param scope
     */
    public ScopedCase(Scope scope) {
        super(scope);
    }
}
