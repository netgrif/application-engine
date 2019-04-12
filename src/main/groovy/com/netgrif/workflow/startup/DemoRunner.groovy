package com.netgrif.workflow.startup

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
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