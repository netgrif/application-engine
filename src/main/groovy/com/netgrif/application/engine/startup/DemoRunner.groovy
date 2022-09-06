package com.netgrif.application.engine.startup

import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
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

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    private static final Logger log = LoggerFactory.getLogger(DemoRunner)

    @Override
    void run(String... args) throws Exception {
        def dashboardNet = helper.createNet("dashboard.xml").orElseThrow()
        def tileNet = helper.createNet("dashboard_tile.xml").orElseThrow()
        def dashboardCase = helper.createCase("My Dashboard", dashboardNet)
        def tile1Case = helper.createCase("Tile 1", tileNet)
        def tile2Case = helper.createCase("Tile 1", tileNet)

        tile1Case.dataSet["x"].value = 0
        tile1Case.dataSet["y"].value = 0
        tile1Case.dataSet["rows"].value = 1
        tile1Case.dataSet["cols"].value = 1
        workflowService.save(tile1Case)
        tile2Case.dataSet["x"].value = 1
        tile2Case.dataSet["y"].value = 1
        tile2Case.dataSet["rows"].value = 1
        tile2Case.dataSet["cols"].value = 1
        workflowService.save(tile2Case)

        dashboardCase.dataSet["rows"].value = 2
        dashboardCase.dataSet["cols"].value = 2
        dashboardCase.dataSet["dashboard"].value = [
                tile1Case.tasks.first().task,
                tile2Case.tasks.first().task
        ]
        workflowService.save(dashboardCase)
        taskService.assignTask(dashboardCase.tasks.first().task)
        taskService.finishTask(dashboardCase.tasks.first().task)
    }
}