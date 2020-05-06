package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.PetriNetService
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
@Component
class FinisherRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FinisherRunner)

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private PetriNetService petriNetService

    @Autowired
    private ImportHelper helper

    @Autowired
    private CaseRepository caseRepository

    @Override
    void run(String... strings) throws Exception {
        helper.createNet("mortgage/address.xml", "major")
        helper.createNet("mortgage/financial_data.xml", "major")
        helper.createNet("mortgage/personal_information.xml", "major")
        def mortgage = helper.createNet("mortgage/mortgage.xml", "major")
        def leukemia = helper.createNet("leukemia.xml", "major")
        superCreator.setAllToSuperUser()

        helper.createCase("Mortgage", mortgage.get())
        helper.createCase("Protocol", leukemia.get())
    }
}