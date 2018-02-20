package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.Organization
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class InsurancePortalImporter extends AbstractOrderedCommandLineRunner {

    private static final Logger log = Logger.getLogger(ImportHelper.class.name)

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private SuperCreator superCreator

    void run(String... strings) {
        log.info("Importing Insurance model")

        def net = importHelper.createNet("insurance_portal_demo.xml", "Insurance Demo", "IPD", superCreator.superUser.transformToLoggedUser())

        assert net.isPresent()

        def org = importHelper.createOrganization("Insurance Company")
        def auths = importHelper.createAuthorities(["user": Authority.user])
        def processRoles = importHelper.getProcessRoles(net.get())

        def user = importHelper.createUser(new User(name: "Agent", surname: "Smith", email: "agent@company.com", password: "password"),
                [auths.get("user")] as Authority[], [org] as Organization[], [processRoles.get("Agent")] as UserProcessRole[])
        importHelper.createUser(new User(name: "Great", surname: "Company", email: "company@company.com", password: "password"),
                [auths.get("user")] as Authority[], [org] as Organization[], [processRoles.get("Company")] as UserProcessRole[])

        30.times { importHelper.createCase("Test ${it + 1}", net.get(), user.transformToLoggedUser()) }
//        30.times { importHelper.createFilter("Test Filter", "{}", "{}",superCreator.superUser.transformToLoggedUser()) }

//        importHelper.createFilter("Test 2", "{\"user\":\"super@netgrif.com\"}", "{\"User\":\"Super Trooper\"}",superCreator.superUser.transformToLoggedUser())

        superCreator.setAllToSuperUser()
    }
}