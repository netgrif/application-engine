package com.netgrif.workflow

import com.netgrif.workflow.premiuminsurance.PostalCodeService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class InsurancePostalCodeImporter {

    private static final Logger log = Logger.getLogger(InsurancePostalCodeImporter.class.name)

    @Value("\${postal.codes.csv}")
    String postalCodesPath

    @Autowired
    private PostalCodeService service

    void run(String... strings) {
        log.info("Importing postal codes from file " + postalCodesPath)
        def importFile = new File(postalCodesPath)
        importFile.splitEachLine(',') { items ->
            service.createPostalCode(items[0], items[1], items[2], items[3])
        }
    }
}
