package com.netgrif.workflow.startup


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
//        helper.createNet("mortgage/address.xml","address","Address","ADD","major")
        def insuranceNet = helper.createNet("insurance_portal_demo.xml","insurance","Insurance","INS","major")
        helper.createNet("leukemia.xml", "protokol_leukemia", "Protokol o začatí a kontrole liečby chronickej myelocytovej leukémie", "LEU", "major")

        helper.createCase("Insurance Case", insuranceNet.get())

        superCreator.setAllToSuperUser()
    }
}