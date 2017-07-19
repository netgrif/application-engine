package com.netgrif.workflow

import com.netgrif.workflow.psc.PostalCodeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class InsurancePostalCodeImporter {

    @Autowired
    private PostalCodeService service

    void run(String... strings) {
        def importFile = new File("src/main/resources/postal_codes.csv")
        importFile.splitEachLine(',') { items ->
            service.createPostalCode(items[0],items[1],items[3])
        }
    }
}
