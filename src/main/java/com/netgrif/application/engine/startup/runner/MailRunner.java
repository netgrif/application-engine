package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.mail.interfaces.IMailService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
@RunnerOrder(170)
@Profile("!test")
@RequiredArgsConstructor
public class MailRunner implements ApplicationEngineStartupRunner {

    private final IMailService service;

    @Override
    public void run(ApplicationArguments strings) throws Exception {
        log.info("Starting test for mail connection");
        host();
        service.testConnection();
    }

    private void host() throws UnknownHostException {
        log.info("HOST ADDRESS: {}", InetAddress.getLocalHost().getHostAddress());
        log.info("HOST NAME: {}", InetAddress.getLocalHost().getHostName());
    }

}
