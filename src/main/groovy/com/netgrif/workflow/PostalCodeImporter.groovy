package com.netgrif.workflow

import com.netgrif.workflow.business.PostalCode
import com.netgrif.workflow.business.PostalCodeService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PostalCodeImporter {

    private static final Logger log = Logger.getLogger(PostalCodeImporter.class.name)

    @Value("\${postal.codes.csv}")
    String postalCodesPath

    @Autowired
    private PostalCodeService service

    void run(String... strings) {
        log.info("Importing postal codes from file " + postalCodesPath)
        def importFile = new File(postalCodesPath)
        def codes = []

        importFile.splitEachLine(',') { items ->
            codes << new PostalCode(items[0].replaceAll("\\s", "").trim(), items[1].trim())
        }
        service.savePostalCodes(codes)
        log.info("Postal codes imported")
    }
}