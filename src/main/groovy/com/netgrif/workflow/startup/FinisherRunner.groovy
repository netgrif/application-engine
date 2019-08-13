package com.netgrif.workflow.startup


import com.netgrif.workflow.petrinet.service.PetriNetService
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static java.util.concurrent.Executors.newFixedThreadPool

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
        def net = helper.createNet("wizard.xml", "a", "a", "a", "major")
        assert net.isPresent()

        Case c = helper.createCase("case", net.get())

        10.times { index ->
            c.title = "Case $index" as String
            caseRepository.save(c)
        }
    }
}