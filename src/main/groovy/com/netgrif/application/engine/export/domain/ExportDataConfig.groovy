package com.netgrif.application.engine.export.domain

import lombok.Data


@Data
class ExportDataConfig {
    LinkedHashSet<String> dataToExport;

}