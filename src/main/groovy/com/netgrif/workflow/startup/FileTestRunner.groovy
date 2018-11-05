package com.netgrif.workflow.startup

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("dev")
@Component
class FileTestRunner extends AbstractOrderedCommandLineRunner {


    @Autowired
    private ImportHelper helper

    @Override
    void run(String... strings) throws Exception {
        def net = helper.createNet("file_test.xml", "test", "File test", "FT", "major").get()

        helper.createCase("File test case", net)
    }
}
