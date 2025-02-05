package com.netgrif.application.engine.action


import com.netgrif.adapter.petrinet.service.PetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.adapter.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(ConcurrencyTest)

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private SuperCreatorRunner superCreator

    @Test
    void test() {
        def mainNet = importHelper.createNet("action_delegate_concurrency_test.xml")
        assert mainNet.get() != null

        List<Case> cases = []
        10.times {
            cases << importHelper.createCase("Case 1", mainNet.get())
        }

        def threads = []
        cases.each { it ->
            String caseId = it.stringId
            threads << new Thread({
                log.info("Running case $caseId")
                importHelper.assignTaskToSuper("task", caseId)
            })
        }

        threads.each {
            it.start()
        }

        threads.each {
            it.join()
        }

        cases.each {
            Optional<Case> caseOptional = caseRepository.findById(it.stringId)
            assert caseOptional.isPresent()
            assert caseOptional.get().stringId == (caseOptional.get().getFieldValue("text") as String)
        }
    }
}
