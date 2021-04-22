package com.netgrif.workflow.export.service.interfaces

import com.netgrif.workflow.export.domain.ExportDataConfig
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task

interface IExportHelperService {

    Set<String> buildDefaultCsvCaseHeader(List<Case> exportCases)

    Set<String> buildDefaultCsvTaskHeader(List<Task> exportTasks)

    OutputStream fillCsvCaseData(File outFile, List<Case> exportCases, ExportDataConfig config)

    OutputStream fillCsvTaskData(File outFile, List<Task> exportTasks, ExportDataConfig config)
}