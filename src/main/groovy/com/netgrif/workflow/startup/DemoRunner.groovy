package com.netgrif.workflow.startup

import com.netgrif.workflow.elastic.domain.ElasticCaseRepository
import com.netgrif.workflow.elastic.service.IElasticCaseService
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Profile("dev")
@Component
class DemoRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ImportHelper helper

    @Override
    void run(String... args) throws Exception {
        def netOptional = helper.createNet("test_model_immediate_data.xml", "TST", "TST", "TST", "major")
        assert netOptional.isPresent()

        5.times {
            helper.createCase("Case $it", netOptional.get())
        }
    }
}