package com.netgrif.application.engine.startup

import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
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
        PetriNet allData = helper.createNet("all_data.xml", VersionType.MAJOR).get()
        PetriNet leukemia = helper.createNet("leukemia.xml", VersionType.MAJOR).get()
        PetriNet leukemiaEn = helper.createNet("leukemia_en.xml", VersionType.MAJOR).get()

        (1..3).forEach {
            helper.createCase("All data " + it, allData)
        }
        (1..5).forEach {
            helper.createCase("Leukemia " + it, leukemia)
        }
        (1..7).forEach {
            helper.createCase("Leukemia en " + it, leukemiaEn)
        }
    }
}