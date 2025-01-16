package com.netgrif.application.engine.workflow.domain;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UseCase extends Case {

    /**
     * todo javadoc
     *
     * @param scope
     */
    public UseCase(Scope scope) {
        super(scope);
    }
}
