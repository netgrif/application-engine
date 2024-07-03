package com.netgrif.application.engine.startup

import com.netgrif.application.engine.business.IPostalCodeService
import com.netgrif.application.engine.business.PostalCode
import com.netgrif.application.engine.business.PostalCodeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
@Profile("!update")
class PostalCodeImporter extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PostalCodeImporter.class.name)

    @Value("\${nae.postal.codes.import}")
    Boolean importPostalCode

    @Value("\${nae.postal.codes.csv}")
    String postalCodesPath

    @Autowired
    private IPostalCodeService service

    @Autowired
    private PostalCodeRepository repository

    void run(String... strings) {
        if (!importPostalCode) {
            log.info("Skip import postal codes")
            return
        }
        log.info("Importing postal codes from file " + postalCodesPath)
        def importFile = new ClassPathResource(postalCodesPath).inputStream

        def lineCount = 0
        importFile.readLines().each {
            lineCount++
        }

        if (repository.count() == lineCount) {
            log.info("All $lineCount postal codes have been already imported")
            return
        }

        repository.deleteAll()

        importFile = new ClassPathResource(postalCodesPath).inputStream
        def codes = []
        importFile.splitEachLine(',') { items ->
            codes << new PostalCode(items[0].replaceAll("\\s", "").trim(), items[1].trim())
        }
        service.savePostalCodes(codes)
        log.info("Postal codes imported")
    }
}