package com.netgrif.workflow.configuration.drools.interfaces;

import org.kie.api.KieBase;

public interface IRefreshableKieBase {

    KieBase kieBase();

    boolean shouldRefresh();

    void refresh();
}
