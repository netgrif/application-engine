package com.netgrif.workflow

import com.netgrif.workflow.psc.PostalCodeService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class InsurancePostalCodeImporter {

    private static final Logger log = Logger.getLogger(InsurancePostalCodeImporter.class.name)

    @Autowired
    private PostalCodeService service

    void run(String... strings) {
        log.info("Importing postal codes")
        def importFile = new File("src/main/resources/postal_codes.csv")
        importFile.splitEachLine(',') { items ->
            service.createPostalCode(items[0],items[1],items[2],items[3])
        }
    }
}
