package com.netgrif.application.engine.configuration.drools.interfaces;

import org.kie.api.KieBase;

public interface IRefreshableKieBase {

    KieBase kieBase();

    boolean shouldRefresh();

    void refresh();
}
