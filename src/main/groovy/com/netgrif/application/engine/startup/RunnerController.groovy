package com.netgrif.application.engine.startup

import org.springframework.stereotype.Component

@Component
class RunnerController {

    private List order = [
            ElasticsearchRunner,
            MongoDbRunner,
            StorageRunner,
            RuleEngineRunner,
            ApplicationRoleRunner,
            AllDataTransitionRunner,
            UriRunner,
            SystemProcessRunner,
            SystemIdentityRunner,
            FunctionsCacheRunner,
            FilterRunner,
            DefaultFiltersRunner,
            DashboardRunner,
            SuperCreator,
            FlushSessionsRunner,
            MailRunner,
            DemoRunner,
            QuartzSchedulerRunner,
            ValidationRunner,
            FinisherRunnerSuperCreator,
            FinisherRunner,
    ]

    protected List getOrderList() {
        return order
    }

    int getOrder(Class aClass) {
        int runnerOrder = getOrderList().findIndexOf { it == aClass }
        if (runnerOrder == -1) {
            throw new IllegalArgumentException("Class ${aClass?.simpleName} is not registered in ${this.class.simpleName}")
        }
        return runnerOrder
    }
}