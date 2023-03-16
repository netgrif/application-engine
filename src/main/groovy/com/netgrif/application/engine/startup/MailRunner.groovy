package com.netgrif.application.engine.startup

import com.netgrif.application.engine.mail.interfaces.IMailService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Slf4j
@Component
@Profile("!test")
@CompileStatic
class MailRunner extends AbstractOrderedCommandLineRunner {

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