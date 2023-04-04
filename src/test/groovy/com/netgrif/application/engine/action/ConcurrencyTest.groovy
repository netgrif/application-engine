package com.netgrif.application.engine.action

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.workflow.domain.Case
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Slf4j
@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class ConcurrencyTest extends EngineTest {

    @Test
    void test() {
        def mainNet = importHelper.createNet("action_delegate_concurrency_test.xml")
        assert mainNet.get() != null

        List<Case> cases = []
        10.times {
            cases << importHelper.createCase("Case 1", mainNet.get())
        }

        List<Thread> threads = []
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
            assert caseOptional.get().stringId == (caseOptional.get().getDataSet().get("text").getRawValue() as String)
        }
    }
}
