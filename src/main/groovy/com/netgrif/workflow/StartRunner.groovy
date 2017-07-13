package com.netgrif.workflow

import com.netgrif.workflow.mail.IMailService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

import java.util.concurrent.ThreadLocalRandom

@Component
@Profile("!test")
class StartRunner  implements CommandLineRunner{

    static Logger log = Logger.getLogger(StartRunner.class.getName())

    @Autowired
    private Environment environment

    @Autowired
    private MongoTemplate mongoTemplate
    @Autowired
    private IMailService mailService

    @Autowired
    private InsuranceImporter insuranceImporter
    @Autowired
    private XlsImporter xlsImporter
    @Autowired
    private SuperCreator superCreator
    @Autowired
    private FlushSessionsRunner sessionsRunner
    @Autowired
    private JMeterExport export



    @Override
    void run(String... strings) throws Exception {
        mongoTemplate.getDb().dropDatabase()

        insuranceImporter.run(strings)

        //xlsImporter.run(strings)

        superCreator.run(strings)

        sessionsRunner.run(strings)
//        export.run(strings)

        mailService.testConnection()
        host()
    }

    private void host(){
        log.info("HOST ADDRESS: "+InetAddress.localHost.hostAddress)
        log.info("HOST NAME: "+InetAddress.localHost.hostName)
    }

    static String randomColor() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 4)
        switch (randomNum) {
            case 0:
                return "color-fg-primary-500"
            case 1:
                return "color-fg-teal-500"
            case 2:
                return "color-fg-deep-purple-600"
            case 3:
                return "color-fg-amber-500"
            case 4:
                return "color-fg-brown-500"
            default:
                return "color-fg-primary-500"
        }
    }
}
