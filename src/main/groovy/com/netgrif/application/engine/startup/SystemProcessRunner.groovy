package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.constants.IdentityConstants
import com.netgrif.application.engine.authorization.domain.constants.ActorConstants;
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class SystemProcessRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ImportHelper helper

    @Override
    void run(String... args) throws Exception {
        Set<ImportData> importData = getSystemProcesses()
        importData.each { processData -> importProcess(processData)  }
    }

    private Set<ImportData> getSystemProcesses() {
        // todo: release/8.0.0 make it automatische
        return [
                new ImportData(IdentityConstants.PROCESS_IDENTIFIER, IdentityConstants.FILE_PATH),
                new ImportData(ActorConstants.PROCESS_IDENTIFIER, ActorConstants.FILE_PATH)
        ] as Set
    }

    /**
     * todo javadoc
     * */
    private void importProcess(ImportData importData) {
        helper.upsertNet(importData.filePath, importData.processIdentifier)
    }

    class ImportData {
        protected String processIdentifier
        protected String filePath

        ImportData(String processIdentifier, String filePath) {
            this.processIdentifier = processIdentifier
            this.filePath = filePath
        }
    }
}
