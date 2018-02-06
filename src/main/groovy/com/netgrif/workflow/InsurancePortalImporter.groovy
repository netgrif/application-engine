package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.Organization
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class InsurancePortalImporter {

    private static final Logger log = Logger.getLogger(ImportHelper.class.name)

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private SuperCreator superCreator

    void run(String... strings) {
        def net = importHelper.createNet("insurance_portal_demo.xml", "Insurance Demo", "IPD", superCreator.superUser.transformToLoggedUser())

        assert net.isPresent()

        def org = importHelper.createOrganization("Insurance Company")
        def auths = importHelper.createAuthorities(["user": Authority.user])
        def processRoles = importHelper.createUserProcessRoles(["agent": "Agent", "company": "Company"], net.get())

        def user = importHelper.createUser(new User(name: "Agent", surname: "Smith", email: "agent@company.com", password: "password"),
                [auths.get("user")] as Authority[], [org] as Organization[], [processRoles.get("agent")] as UserProcessRole[])
        importHelper.createUser(new User(name: "Great", surname: "Company", email: "company@company.com", password: "password"),
                [auths.get("user")] as Authority[], [org] as Organization[], [processRoles.get("company")] as UserProcessRole[])

        5.times { importHelper.createCase("Test ${it + 1}", net.get(), user.transformToLoggedUser()) }
    }
}
