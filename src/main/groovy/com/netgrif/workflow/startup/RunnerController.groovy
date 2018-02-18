package com.netgrif.workflow.startup

import org.springframework.stereotype.Component

@Component
class RunnerController {

    private List order = [
            MongoDbRunner,
            StorageRunner,
            DefaultRoleRunner,
            SuperCreator,
            FlushSessionsRunner,
            InsurancePortalImporter,
            MailRunner,
            PostalCodeImporter,
    ]

    int getOrder(Class aClass) {
        int runnerOrder = order.findIndexOf { it == aClass }
        if (runnerOrder == -1) {
            throw new IllegalArgumentException("Class ${aClass.simpleName} is not registered in ${this.class.simpleName}")
        }
        return runnerOrder
    }
}