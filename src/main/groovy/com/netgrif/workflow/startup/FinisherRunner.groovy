package com.netgrif.workflow.startup

import com.netgrif.workflow.petrinet.service.PetriNetService
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.elastic.ElastiCase
import com.netgrif.workflow.workflow.domain.elastic.ElastiCaseRepository
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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
    private ElastiCaseRepository repository

    @Autowired
    private CaseRepository caseRepository

    @Override
    void run(String... strings) throws Exception {
        superCreator.setAllToSuperUser()

        long caseCount = caseRepository.count()
        long numOfPages = ((caseCount/100.0) +1) as long
        log.info("Processing cases: $numOfPages pages")
        numOfPages.times { page ->
            log.info("Page $page / $numOfPages")

            Page<Case> cases = caseRepository.findAll(new PageRequest(page, 100))

            def elastic = []
            cases.each {
                elastic << new ElastiCase(it)
            }
            repository.saveAll(elastic)
        }

    }
}