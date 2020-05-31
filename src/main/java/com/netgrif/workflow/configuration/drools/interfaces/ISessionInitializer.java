package com.netgrif.workflow.configuration.drools.interfaces;

import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

@Service
public interface ISessionInitializer {

    void setGlobals(KieSession session);
}
