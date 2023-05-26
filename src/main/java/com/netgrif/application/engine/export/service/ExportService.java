package com.netgrif.application.engine.export.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.export.configuration.ExportConfiguration;
import com.netgrif.application.engine.export.domain.ExportDataConfig;
import com.netgrif.application.engine.export.service.interfaces.IExportService;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportService implements IExportService {

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IElasticCaseService elasticCaseService;

    @Autowired
    private IElasticTaskService elasticTaskService;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ExportConfiguration exportConfiguration;

    @Autowired
    private IUserService userService;


    @Override
    public Set<String> buildDefaultCsvCaseHeader(List<Case> exportCases) {
        Set<String> header = new LinkedHashSet<>();
        exportCases.forEach(exportCase ->
                header.addAll(exportCase.getImmediateDataFields())
        );
        return header;
    }

    @Override
    public Set<String> buildDefaultCsvTaskHeader(List<Task> exportTasks) {
        Set<String> header = new LinkedHashSet<>();
        exportTasks.forEach(
                exportTask ->
                        header.addAll(exportTask.getImmediateDataFields())
        );
        return header;
    }

    @Override
    public OutputStream fillCsvCaseData(Predicate predicate, File outFile) throws FileNotFoundException {
        return fillCsvCaseData(predicate, outFile, null, exportConfiguration.getMongoPageSize());
    }

    @Override
    public OutputStream fillCsvCaseData(Predicate predicate, File outFile, ExportDataConfig config) throws FileNotFoundException {
        return fillCsvCaseData(predicate, outFile, config, exportConfiguration.getMongoPageSize());
    }

    @Override
    public OutputStream fillCsvCaseData(Predicate predicate, File outFile, ExportDataConfig config, int pageSize) throws FileNotFoundException {
        int numOfPages = (int) (((caseRepository.count(predicate)) / pageSize) + 1);
        List<Case> exportCases = new ArrayList<>();
        for (int i = 0; i < numOfPages; i++) {
            exportCases.addAll(workflowService.search(predicate, PageRequest.of(i, pageSize)).getContent());
        }
        return buildCaseCsv(exportCases, config, outFile);
    }

    @Override
    public OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile) throws FileNotFoundException {
        return fillCsvCaseData(requests, outFile, null, userService.getLoggedOrSystem().transformToLoggedUser(), exportConfiguration.getMongoPageSize(), LocaleContextHolder.getLocale(), false);
    }

    @Override
    public OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config) throws FileNotFoundException {
        return fillCsvCaseData(requests, outFile, config, userService.getLoggedOrSystem().transformToLoggedUser(), exportConfiguration.getMongoPageSize(), LocaleContextHolder.getLocale(), false);
    }

    @Override
    public OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config,
                                        LoggedUser user) throws FileNotFoundException {
        return fillCsvCaseData(requests, outFile, config, user, exportConfiguration.getMongoPageSize(), LocaleContextHolder.getLocale(), false);
    }

    @Override
    public OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config,
                                        LoggedUser user, int pageSize) throws FileNotFoundException {
        return fillCsvCaseData(requests, outFile, config, user, pageSize, LocaleContextHolder.getLocale(), false);
    }

    @Override
    public OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config,
                                        LoggedUser user, int pageSize, Locale locale) throws FileNotFoundException {
        return fillCsvCaseData(requests, outFile, config, user, pageSize, locale, false);
    }

    @Override
    public OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config,
                                        LoggedUser user, int pageSize, Locale locale, Boolean isIntersection) throws FileNotFoundException {
        int numOfPages = (int) ((elasticCaseService.count(requests, user, locale, isIntersection) / pageSize) + 1);
        List<Case> exportCases = new ArrayList<>();
        for (int i = 0; i < numOfPages; i++) {
            exportCases.addAll(elasticCaseService.search(requests, user, PageRequest.of(i, pageSize), locale, isIntersection).toList());
        }
        return buildCaseCsv(exportCases, config, outFile);
    }

    @Override
    public OutputStream buildCaseCsv(List<Case> exportCases, ExportDataConfig config, File outFile) throws FileNotFoundException {
        Set<String> csvHeader = config == null ? buildDefaultCsvCaseHeader(exportCases) : config.getDataToExport();
        OutputStream outStream = new FileOutputStream(outFile, false);
        PrintWriter writer;
        if (config == null || config.getStandardCharsets() == null) {
            writer = new PrintWriter(outStream, true, StandardCharsets.UTF_8);
        } else {
            writer = new PrintWriter(outStream, true, config.getStandardCharsets());
        }
        writer.println(String.join(",", csvHeader));
        for (Case exportCase : exportCases) {
            writer.println(String.join(",", buildRecord(csvHeader, exportCase)).replace("\n", "\\n"));
        }
        writer.close();
        return outStream;
    }

    @Override
    public OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile) throws FileNotFoundException {
        return fillCsvTaskData(requests, outFile, null, userService.getLoggedOrSystem().transformToLoggedUser(), exportConfiguration.getMongoPageSize(), LocaleContextHolder.getLocale(), false);
    }

    @Override
    public OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config) throws FileNotFoundException {
        return fillCsvTaskData(requests, outFile, config, userService.getLoggedOrSystem().transformToLoggedUser(), exportConfiguration.getMongoPageSize(), LocaleContextHolder.getLocale(), false);
    }

    @Override
    public OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config,
                                        LoggedUser user) throws FileNotFoundException {
        return fillCsvTaskData(requests, outFile, config, user, exportConfiguration.getMongoPageSize(), LocaleContextHolder.getLocale(), false);
    }

    @Override
    public OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config,
                                        LoggedUser user, int pageSize) throws FileNotFoundException {
        return fillCsvTaskData(requests, outFile, config, user, pageSize, LocaleContextHolder.getLocale(), false);
    }

    @Override
    public OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config,
                                        LoggedUser user, int pageSize, Locale locale) throws FileNotFoundException {
        return fillCsvTaskData(requests, outFile, config, user, pageSize, locale, false);
    }

    @Override
    public OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config,
                                        LoggedUser user, int pageSize, Locale locale, Boolean isIntersection) throws FileNotFoundException {
        int numberOfTasks = (int) ((elasticTaskService.count(requests, user, locale, isIntersection) / pageSize) + 1);
        List<Task> exportTasks = new ArrayList<>();

        for (int i = 0; i < numberOfTasks; i++) {
            exportTasks.addAll(elasticTaskService.search(requests, user, PageRequest.of(0, pageSize), locale, isIntersection).toList());
        }
        return buildTaskCsv(exportTasks, config, outFile);
    }

    @Override
    public OutputStream fillCsvTaskData(Predicate predicate, File outFile) throws FileNotFoundException {
        return fillCsvTaskData(predicate, outFile, null, exportConfiguration.getMongoPageSize());
    }

    @Override
    public OutputStream fillCsvTaskData(Predicate predicate, File outFile, ExportDataConfig config) throws FileNotFoundException {
        return fillCsvTaskData(predicate, outFile, config, exportConfiguration.getMongoPageSize());
    }

    @Override
    public OutputStream fillCsvTaskData(Predicate predicate, File outFile, ExportDataConfig config, int pageSize) throws FileNotFoundException {
        int numberOfTasks = (int) taskRepository.count(predicate);
        List<Task> exportTasks = new ArrayList<>();
        for (int i = 0; i < numberOfTasks; i++) {
            exportTasks.addAll(taskService.search(predicate, PageRequest.of(i, pageSize)).getContent());
        }
        return buildTaskCsv(exportTasks, config, outFile);
    }

    @Override
    public OutputStream buildTaskCsv(List<Task> exportTasks, ExportDataConfig config, File outFile) throws FileNotFoundException {
        Set<String> csvHeader = config == null ? buildDefaultCsvTaskHeader(exportTasks) : config.getDataToExport();
        OutputStream outStream = new FileOutputStream(outFile, false);
        PrintWriter writer = new PrintWriter(outStream, true);
        writer.println(String.join(",", csvHeader));
        for (Task exportTask : exportTasks) {
            Case taskCase = workflowService.findOne(exportTask.getCaseId());
            writer.println(String.join(",", buildRecord(csvHeader, taskCase)).replace("\n", "\\n"));
        }
        writer.close();
        return outStream;
    }

    @Override
    public List<String> buildRecord(Set<String> csvHeader, Case exportCase) {
        List<String> recordStringList = new LinkedList<>();
        for (String dataFieldId : csvHeader) {
            if (exportCase.getDataSet().containsKey(dataFieldId)) {
                recordStringList.add(StringEscapeUtils.escapeCsv(resolveFieldValue(exportCase, dataFieldId)));
            } else
                recordStringList.add("");
        }
        return recordStringList;
    }

    @Override
    public String resolveFieldValue(Case exportCase, String exportFieldId) {
        String fieldValue;
        Field field = exportCase.getField(exportFieldId);
        Object fieldData = exportCase.getDataField(exportFieldId).getValue();
        if (field.getValue() == null && exportCase.getDataSet().get(exportFieldId).getValue() == null) {
            return "";
        }
        switch (field.getType()) {
            case MULTICHOICE_MAP:
                fieldValue = ((MultichoiceMapField) fieldData).getValue().stream()
                        .filter(value -> ((MultichoiceMapField) fieldData).getOptions().containsKey(value.trim()))
                        .map(value -> ((MultichoiceMapField) fieldData).getOptions().get(value.trim()).getDefaultValue())
                        .collect(Collectors.joining(","));
                break;
            case ENUMERATION_MAP:
                fieldValue = ((EnumerationMapField) fieldData).getOptions().get(fieldData).getDefaultValue();
                break;
            case MULTICHOICE:
                fieldValue = String.join(",", ((List<I18nString>) fieldData).stream().map(I18nString::toString).collect(Collectors.toList()));
                break;
            case FILE:
                fieldValue = ((FileField) fieldData).getValue().toString();
                break;
            case FILELIST:
                fieldValue = String.join(",", ((FileListField) fieldData).getValue().getNamesPaths().stream().map(FileFieldValue::toString).collect(Collectors.toSet()));
                break;
            case TASK_REF:
                fieldValue = String.join(";", ((TaskField) fieldData).getValue());
                break;
            case USER:
                fieldValue = ((UserFieldValue) fieldData).getEmail();
                break;
            case DATE:
                fieldValue = ((LocalDate) fieldData).toString();
                break;
            case DATETIME:
                fieldValue = ((Date) fieldData).toString();
                break;
            case USERLIST:
                fieldValue = ((UserListField) fieldData).getValue().getUserValues().stream().map(UserFieldValue::getId).collect(Collectors.joining(";"));
                break;
            case NUMBER:
                fieldValue = fieldData.toString();
                break;
            default:
                fieldValue = fieldData == null ? (String) exportCase.getDataSet().get(exportFieldId).getValue() : (String) fieldData;
                break;
        }
        return fieldValue;
    }
}
