package com.netgrif.application.engine.export.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

/**
 * Configuration properties for XLS export functionality.
 * <p>
 * Properties are prefixed with <code>nae.xls.export</code> in the configuration files
 * (e.g., <code>application.yml</code> or <code>application.properties</code>).
 * <p>
 * These settings control the behavior and formatting of exported Excel files.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "nae.xls.export")
public class XlsExportProperties {

    /**
     * The default name of the exported XLS file (without extension).
     * <p>
     * Example: <code>export</code>
     */
    private String exportFileName = "export";

    /**
     * The name of the sheet inside the exported XLS file.
     * <p>
     * Example: <code>export</code>
     */
    private String sheetName = "export";

    /**
     * The maximum number of rows allowed in the export.
     * <p>
     * Must be 0 or greater. If set to 0, row limit is effectively disabled.
     * Default is 10,000.
     */
    @Min(0)
    private long maxRows = 10000L;

    /**
     * The number of records per export page.
     * <p>
     * Must be greater than 0. Default is 100.
     */
    @Min(1)
    private int pageSize = 100;

    /**
     * Whether to include metadata (such as case stringId, case author) in the exported document.
     * <p>
     * Default is <code>true</code>.
     */
    private boolean addMetaData = true;

    /**
     * Whether to export all immediate fields of exported cases.
     * <p>
     * Default is <code>true</code>.
     */
    private boolean exportAllImmediateFields = true;

    /**
     * Warning message shown when the number of exported records exceeds the configured limit.
     * <p>
     * Default: <br>
     * <code>Tento dokument obsahuje maximálny povolený počet záznamov pre export.
     * Pre zvýšenie limitu exportu záznamov prosím kontaktuje svojho administrátora.</code>
     */
    private String trimWarningMessage = "Tento dokument obsahuje maximálny povolený počet záznamov pre export. Pre zvýšenie limitu exportu záznamov prosím kontaktuje svojho administrátora.";

    /**
     * Date format used in the exported XLS file.
     * <p>
     * Example: <code>dd.MM.yyyy</code>
     */
    private String datePattern = "dd.MM.yyyy";

    /**
     * Date-time format used in the exported XLS file.
     * <p>
     * Example: <code>dd.MM.yyyy HH:mm:ss</code>
     */
    private String dateTimePattern = "dd.MM.yyyy HH:mm:ss";

}
