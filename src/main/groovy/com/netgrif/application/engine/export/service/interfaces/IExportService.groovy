//package com.netgrif.application.engine.export.service.interfaces
//
//import com.netgrif.application.engine.auth.domain.LoggedUser
//import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
//import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest
//import com.netgrif.application.engine.export.domain.ExportDataConfig
//import com.netgrif.application.engine.workflow.domain.Case
//import com.netgrif.application.engine.workflow.domain.Task
//import com.querydsl.core.types.Predicate
//
//interface IExportService {
//
//    Set<String> buildDefaultCsvCaseHeader(List<Case> exportCases)
//
//    Set<String> buildDefaultCsvTaskHeader(List<Task> exportTasks)
//
//    OutputStream fillCsvCaseData(Closure<Predicate> predicate, File outFile)
//
//    OutputStream fillCsvCaseData(Closure<Predicate> predicate, File outFile, ExportDataConfig config)
//
//    OutputStream fillCsvCaseData(Closure<Predicate> predicate, File outFile, ExportDataConfig config, int pageSize)
//
//    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile)
//
//    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config)
//
//    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user)
//
//    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize)
//
//    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize, Locale locale)
//
//    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize, Locale locale, Boolean isIntersection)
//
//    OutputStream fillCsvTaskData(Closure<Predicate> predicate, File outFile)
//
//    OutputStream fillCsvTaskData(Closure<Predicate> predicate, File outFile, ExportDataConfig config)
//
//    OutputStream fillCsvTaskData(Closure<Predicate> predicate, File outFile, ExportDataConfig config, int pageSize)
//
//    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile)
//
//    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config)
//
//    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user)
//
//    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize)
//
//    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize, Locale locale)
//
//    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize, Locale locale, Boolean isIntersection)
//
//}