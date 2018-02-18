package com.netgrif.workflow.startup

import com.netgrif.workflow.mail.MailService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class MailRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = Logger.getLogger(MailRunner)

    @Autowired
    private MailService service

    @Override
    void run(String... strings) throws Exception {
        log.info("Starting test for mail connection")
        host()
        service.testConnection()
    }

    private void host() {
        log.info("HOST ADDRESS: " + InetAddress.localHost.hostAddress)
        log.info("HOST NAME: " + InetAddress.localHost.hostName)
    }
}
