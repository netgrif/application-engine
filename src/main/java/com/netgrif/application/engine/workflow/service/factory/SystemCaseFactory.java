package com.netgrif.application.engine.workflow.service.factory;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.SystemCase;

import javax.annotation.PostConstruct;

public abstract class SystemCaseFactory<T extends SystemCase> {
    /**
     * todo javadoc
     * */
    public abstract T createObject(Case systemCase);

    /**
     * todo javadoc
     * */
    @PostConstruct
    protected abstract void registerFactory();

}
