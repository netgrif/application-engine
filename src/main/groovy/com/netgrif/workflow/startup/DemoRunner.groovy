package com.netgrif.workflow.startup


import com.netgrif.workflow.elastic.domain.ElasticCaseRepository
import com.netgrif.workflow.elastic.domain.ElasticTaskRepository
import com.netgrif.workflow.elastic.service.IElasticCaseService
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
        log.info("Elastic")

//        Predicate filter = QCase.case$.title.isNotNull()
//        long caseCount = caseRepository.count(filter)
//        long numOfPages = ((caseCount/100.0)+1) as Long
//        numOfPages.times { page->
//            log.info("Page ${page +1} / $numOfPages")
//            Page<Case> cases = caseRepository.findAll(filter, new PageRequest(page, 100))
//            cases.getContent().eachWithIndex { useCase, index ->
//                log.info("Case $index")
//                def elastiCases = []
//                1000.times {
//                    ElasticCase elasticCase = new ElasticCase(useCase)
//                    elasticCase.mongoId = new ObjectId() as String
//                    elastiCases << elasticCase
//                }
//                repository.saveAll elastiCases
//            }
//        }

//        Predicate filter = QTask.task.title.isNotNull()
//            long taskCount = taskRepository.count(filter)
//            long numOfPages = ((taskCount / 100.0) + 1) as long
//            numOfPages.times { page ->
//                log.info("Page $page / $numOfPages")
//                Page<Task> tasks = taskRepository.findAll(filter, new PageRequest(page, 100))
//                tasks.eachWithIndex { Task task, int index ->
//                    log.info("Case $index")
//                    def elasticTasks = []
//                    80000.times {
//                        ElasticTask elasticTask = new ElasticTask(task)
//                        elasticTask.id = new ObjectId() as String
//                        elasticTasks << elasticTask
//                    }
//                    elasticTaskRepository.saveAll elasticTasks
//                }
//        }
    }
}