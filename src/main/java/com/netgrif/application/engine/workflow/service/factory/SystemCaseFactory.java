package com.netgrif.application.engine.workflow.service.factory;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.SystemCase;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
public abstract class SystemCaseFactory<T extends SystemCase> {

    protected final SystemCaseFactoryRegistry factoryRegistry;

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
