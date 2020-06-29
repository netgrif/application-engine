package com.netgrif.workflow.startup

import com.netgrif.workflow.configuration.drools.interfaces.IRefreshableKieBase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RuleEngineRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineRunner)
    
    @Autowired
    private IRefreshableKieBase refreshableKieBase

    @Override
    void run(String... strings) throws Exception {
        refreshableKieBase.refresh()
    }

}