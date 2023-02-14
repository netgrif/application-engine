package com.netgrif.application.engine.export.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.export.domain.ExportDataConfig;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.querydsl.core.types.Predicate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface IExportService {

    Set<String> buildDefaultCsvCaseHeader(List<Case> exportCases);

    Set<String> buildDefaultCsvTaskHeader(List<Task> exportTasks);

    OutputStream fillCsvCaseData(Predicate predicate, File outFile)  throws FileNotFoundException;

    OutputStream fillCsvCaseData(Predicate predicate, File outFile, ExportDataConfig config)  throws FileNotFoundException;

    OutputStream fillCsvCaseData(Predicate predicate, File outFile, ExportDataConfig config, int pageSize)  throws FileNotFoundException;

    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile)  throws FileNotFoundException ;

    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config) throws FileNotFoundException ;

    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user) throws FileNotFoundException;

    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize)  throws FileNotFoundException;

    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize, Locale locale) throws FileNotFoundException;

    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize, Locale locale, Boolean isIntersection) throws FileNotFoundException;

    OutputStream fillCsvTaskData(Predicate predicate, File outFile) throws FileNotFoundException;

    OutputStream fillCsvTaskData(Predicate predicate, File outFile, ExportDataConfig config) throws FileNotFoundException;

    OutputStream fillCsvTaskData(Predicate predicate, File outFile, ExportDataConfig config, int pageSize)  throws FileNotFoundException;

    OutputStream buildCaseCsv(List<Case> exportCases, ExportDataConfig config, File outFile) throws FileNotFoundException;

    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile) throws FileNotFoundException;

    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config) throws FileNotFoundException;

    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user) throws FileNotFoundException;

    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize) throws FileNotFoundException;

    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize, Locale locale) throws FileNotFoundException;

    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config, LoggedUser user, int pageSize, Locale locale, Boolean isIntersection) throws FileNotFoundException;

    OutputStream buildTaskCsv(List<Task> exportTasks, ExportDataConfig config, File outFile) throws FileNotFoundException;

    List<String> buildRecord(Set<String> csvHeader, Case exportCase);

    String resolveFieldValue(Case exportCase, String exportFieldId);
}