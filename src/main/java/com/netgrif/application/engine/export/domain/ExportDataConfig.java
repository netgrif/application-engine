package com.netgrif.application.engine.export.domain;

import lombok.Data;

import java.nio.charset.Charset;
import java.util.LinkedHashSet;

@Data
public class ExportDataConfig {

    LinkedHashSet<String> dataToExport;

    Charset standardCharsets;

}
