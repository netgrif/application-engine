package com.netgrif.application.engine.export.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.xls.export")
public class XlsExportProperties {

    private String exportFileName = "export";
    private String sheetName = "export";
    private long maxRows = 10000L;
    private int pageSize = 100;
    private boolean addMetaData = true;
    private String trimWarningMessage = "Tento dokument obsahuje maximálny povolený počet záznamov pre export. Pre zvýšenie limitu exportu záznamov prosím kontaktuje svojho administrátora.";
    private String datePattern = "dd.MM.yyyy";
    private String dateTimePattern = "dd.MM.yyyy HH:mm:ss";
    private boolean exportAllImmediateFields = true;

}
