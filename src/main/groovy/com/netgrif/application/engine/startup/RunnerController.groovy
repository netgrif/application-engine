package com.netgrif.application.engine.startup

import org.springframework.stereotype.Component

@Component
class RunnerController {

    private List order = [
            ElasticsearchRunner,
            MongoDbRunner,
            StorageRunner,
            RuleEngineRunner,
            DefaultRoleRunner,
            AnonymousRoleRunner,
            AuthorityRunner,
            SystemUserRunner,
            UriRunner,
            FunctionsCacheRunner,
            FilterRunner,
            GroupRunner,
            DefaultFiltersRunner,
            ImpersonationRunner,
            DashboardRunner,
            SuperCreator,
            FlushSessionsRunner,
            MailRunner,
            PostalCodeImporter,
            DemoRunner,
            QuartzSchedulerRunner,
            PdfRunner,
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