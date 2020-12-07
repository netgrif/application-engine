package com.netgrif.workflow.startup

import com.netgrif.workflow.elastic.domain.ElasticCaseRepository
import com.netgrif.workflow.elastic.domain.ElasticTaskRepository
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("dev")
@Component
class DemoRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ImportHelper helper

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private IElasticCaseService caseService

    @Autowired
    private ElasticCaseRepository repository

    @Autowired
    private ElasticTaskRepository elasticTaskRepository

    private static final Logger log = LoggerFactory.getLogger(DemoRunner)

    @Override
    void run(String... args) throws Exception {
    }
}