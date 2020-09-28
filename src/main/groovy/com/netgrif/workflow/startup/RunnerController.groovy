package com.netgrif.workflow.startup

import org.springframework.stereotype.Component

@Component
class RunnerController {

    private List order = [
            ElasticsearchRunner,
            MongoDbRunner,
            Neo4jRunner,
            StorageRunner,
            RuleEngineRunner,
            DefaultRoleRunner,
            DefaultGroupRunner,
            AuthorityRunner,
            SuperCreator,
            SystemUserRunner,
            FlushSessionsRunner,
            MailRunner,
            PostalCodeImporter,
            DummyUserRunner,
            DemoRunner,
            GroupRunner,
            QuartzSchedulerRunner,
            FinisherRunner
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