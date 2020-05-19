package com.netgrif.workflow.configuration.drools.interfaces;

import org.kie.api.KieBase;
import org.springframework.stereotype.Service;

@Service
public interface IKnowledgeBaseInitializer {

    KieBase constructKieBase();

}
