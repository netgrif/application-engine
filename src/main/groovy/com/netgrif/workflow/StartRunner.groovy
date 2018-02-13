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

    @Override
    void run(String... strings) throws Exception {
        mongoTemplate.getDb().dropDatabase()

        log.info("Creating storage folder")
        File storage = new File("storage/generated/start.txt")
        storage.getParentFile().mkdirs()

        defaultRoleRunner.run()
        superCreator.run(strings)
        sessionsRunner.run(strings)

        insurancePortalImporter.run(strings)

        superCreator.setAllToSuperUser()

        log.info("Starting test for mail connection")
        mailService.testConnection()
        host()
    }

    private static void host() {
        log.info("HOST ADDRESS: " + InetAddress.localHost.hostAddress)
        log.info("HOST NAME: " + InetAddress.localHost.hostName)
    }
}
