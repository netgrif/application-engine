package com.netgrif.workflow.export.domain

import lombok.Data

@Data
class ExportDataConfig {

    LinkedHashSet<String> dataToExport
}
