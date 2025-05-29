package com.netgrif.application.engine.transaction.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.transaction")
public class NaeTransactionProperties {
    private boolean createCaseTransactional = true;
    private boolean deleteCaseTransactional = true;
    private boolean setDataTransactional = true;
    private boolean getDataTransactional = true;
    private boolean taskEventTransactional = true;
}
