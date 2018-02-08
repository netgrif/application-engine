package com.netgrif.workflow

import com.netgrif.workflow.mail.IMailService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

import java.util.concurrent.ThreadLocalRandom

@Component
@Order(value = 1)
@Profile("!test")
class StartRunner implements CommandLineRunner {

    private static Logger log = Logger.getLogger(StartRunner.class.getName())

    @Autowired
    private Environment environment

    @Autowired
    private MongoTemplate mongoTemplate

    @Autowired
    private IMailService mailService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private FlushSessionsRunner sessionsRunner

    @Autowired
    private InsurancePortalImporter insurancePortalImporter

    @Autowired
    private JMeterExport export

    @Autowired
    private DefaultRoleRunner defaultRoleRunner

    @Autowired
    private PostalCodeImporter postalCodeImporter

    @Override
    void run(String... strings) throws Exception {
        mongoTemplate.getDb().dropDatabase()

        log.info("Creating storage folder")
        File storage = new File("storage/generated/start.txt")
        storage.getParentFile().mkdirs()

        defaultRoleRunner.run()
        insurancePortalImporter.run(strings)
        superCreator.run(strings)
        sessionsRunner.run(strings)

        log.info("Starting test for mail connection")
        mailService.testConnection()

        mailService.sendRegistrationEmail("mazari@netgrif.com", "token")

        postalCodeImporter.run(strings)

        host()
    }

    private void host() {
        log.info("HOST ADDRESS: " + InetAddress.localHost.hostAddress)
        log.info("HOST NAME: " + InetAddress.localHost.hostName)
    }

    static String randomColor() {
        return "color-fg-accent-50"

        int randomNum = ThreadLocalRandom.current().nextInt(0, 4)
        switch (randomNum) {
            case 0:
                return "color-fg-primary-500"
            case 1:
                return "color-fg-teal-500"
            case 2:
                return "color-fg-deep-orange-500"
            case 3:
                return "color-fg-amber-500"
            case 4:
                return "color-fg-brown-500"
            default:
                return "color-fg-primary-500"
        }
    }
}
