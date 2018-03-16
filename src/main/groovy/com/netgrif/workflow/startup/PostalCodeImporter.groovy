package com.netgrif.workflow.startup

import com.netgrif.workflow.business.IPostalCodeService
import com.netgrif.workflow.business.PostalCode
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
@Profile("!update")
class PostalCodeImporter extends AbstractOrderedCommandLineRunner {

    private static final Logger log = Logger.getLogger(PostalCodeImporter.class.name)

    @Value("\${postal.codes.csv}")
    String postalCodesPath

    @Autowired
    private IPostalCodeService service

    void run(String... strings) {
        log.info("Importing postal codes from file " + postalCodesPath)
        def importFile = new ClassPathResource(postalCodesPath).inputStream
        def codes = []

        importFile.splitEachLine(',') { items ->
            codes << new PostalCode(items[0].replaceAll("\\s", "").trim(), items[1].trim())
        }
        service.savePostalCodes(codes)
        log.info("Postal codes imported")
    }
}