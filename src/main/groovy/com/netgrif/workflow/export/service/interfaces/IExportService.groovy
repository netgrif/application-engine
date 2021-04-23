package com.netgrif.workflow.export.service.interfaces

import com.netgrif.workflow.auth.domain.LoggedUser
import com.netgrif.workflow.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.workflow.elastic.web.requestbodies.ElasticTaskSearchRequest
import com.netgrif.workflow.export.domain.ExportDataConfig
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
import com.querydsl.core.types.Predicate

interface IExportService {

    Set<String> buildDefaultCsvCaseHeader(List<Case> exportCases)

    Set<String> buildDefaultCsvTaskHeader(List<Task> exportTasks)

    OutputStream fillCsvCaseData(Closure<Predicate> predicate, File outFile, ExportDataConfig config, int pageSize)

    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config,
                                 LoggedUser user,int pageSize,Locale locale,Boolean isIntersection)

    OutputStream fillCsvTaskData(Closure<Predicate> predicate, File outFile, ExportDataConfig config, int pageSize)

    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config,
                                 LoggedUser user, int pageSize, Locale locale, Boolean isIntersection)
}