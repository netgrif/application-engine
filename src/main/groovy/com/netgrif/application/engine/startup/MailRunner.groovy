package com.netgrif.application.engine.startup

import com.netgrif.application.engine.mail.interfaces.IMailService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class MailRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MailRunner)

    @Autowired
    private IMailService service

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