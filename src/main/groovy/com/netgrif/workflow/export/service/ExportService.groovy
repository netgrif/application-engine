package com.netgrif.workflow.export.service

import com.netgrif.workflow.auth.domain.LoggedUser
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService
import com.netgrif.workflow.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.workflow.elastic.web.requestbodies.ElasticTaskSearchRequest
import com.netgrif.workflow.export.configuration.ExportConfiguration
import com.netgrif.workflow.export.domain.ExportDataConfig
import com.netgrif.workflow.export.service.interfaces.IExportService
import com.netgrif.workflow.petrinet.domain.dataset.*
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.QCase
import com.netgrif.workflow.workflow.domain.QTask
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringEscapeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

import java.util.stream.Collectors

@Service
@Slf4j
class ExportService implements IExportService {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private IElasticTaskService elasticTaskService

    @Autowired
    private ExportConfiguration exportConfiguration

    @Autowired
    private IUserService userService

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private ITaskService taskService

    @Autowired
    private TaskRepository taskRepository


    @Override
    Set<String> buildDefaultCsvCaseHeader(List<Case> exportCases) {
        Set<String> header = new LinkedHashSet<>()
        exportCases.forEach { exportCase ->
            header.addAll(exportCase.immediateDataFields)
        }
        return header
    }

    @Override
    Set<String> buildDefaultCsvTaskHeader(List<Task> exportTasks) {
        Set<String> header = new LinkedHashSet<>()
        exportTasks.forEach { exportTask ->
            header.addAll(exportTask.immediateDataFields)
        }
        return header
    }

    @Override
    OutputStream fillCsvCaseData(Closure<Predicate> predicate, File outFile, ExportDataConfig config = null,
                                 int pageSize = exportConfiguration.getMongoPageSize()) {
        QCase qCase = new QCase("case")
        int numOfPages = (caseRepository.count(predicate(qCase)) / pageSize) + 1
        List<Case> exportCases = new ArrayList<>()
        for (int i in 0..numOfPages) {
            exportCases.addAll(workflowService.search(predicate(qCase), PageRequest.of(i, pageSize)).content)
        }
        return buildCaseCsv(exportCases, config, outFile)
    }

    @Override
    OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config = null,
                                 LoggedUser user = userService.loggedOrSystem.transformToLoggedUser(),
                                 int pageSize = exportConfiguration.getMongoPageSize(),
                                 Locale locale = LocaleContextHolder.getLocale(),
                                 Boolean isIntersection = false) {
        int numOfPages = (elasticCaseService.count(requests, user, locale, isIntersection) / pageSize) + 1
        List<Case> exportCases = new ArrayList<>()
        for (int i in 0..<numOfPages) {
            exportCases.addAll(elasticCaseService.search(requests, user, PageRequest.of(i, pageSize), locale, isIntersection).toList())
        }
        return buildCaseCsv(exportCases, config, outFile)
    }

    private OutputStream buildCaseCsv(List<Case> exportCases, ExportDataConfig config, File outFile){
        Set<String> csvHeader = config == null ? buildDefaultCsvCaseHeader(exportCases) : config.getDataToExport()
        OutputStream outStream = new FileOutputStream(outFile, false)
        PrintWriter writer = new PrintWriter(outStream, true)
        writer.println(csvHeader.join(","))
        exportCases.forEach { exportCase ->
            writer.println(buildRecord(csvHeader, exportCase).join(",").replace("\n", "\\n"))
        }
        writer.close()
        return outStream
    }

    @Override
    OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config = null,
                                 LoggedUser user = userService.loggedOrSystem.transformToLoggedUser(),
                                 int pageSize = exportConfiguration.getMongoPageSize(),
                                 Locale locale = LocaleContextHolder.getLocale(),
                                 Boolean isIntersection = false) {
        int numberOfTasks = (elasticTaskService.count(requests, user, locale, isIntersection) / pageSize) + 1
        List<Task> exportTasks = new ArrayList<>()
        for (i in 0..numberOfTasks) {
            exportTasks.addAll(elasticTaskService.search(requests, user, PageRequest.of(0, pageSize), locale, isIntersection).toList())
        }
        return buildTaskCsv(exportTasks, config, outFile)
    }

    @Override
    OutputStream fillCsvTaskData(Closure<Predicate> predicate, File outFile, ExportDataConfig config = null
                                 , int pageSize = exportConfiguration.getMongoPageSize()) {
        QTask qTask = new QTask("task")
        int numberOfTasks = taskRepository.count(predicate(qTask))
        List<Task> exportTasks = new ArrayList<>()
        for (i in 0..numberOfTasks) {
            exportTasks.addAll(taskService.search(predicate(qTask), PageRequest.of(i, pageSize)).content)
        }
        return buildTaskCsv(exportTasks, config, outFile)
    }

    private OutputStream buildTaskCsv(List<Task> exportTasks, ExportDataConfig config, File outFile){
        Set<String> csvHeader = config == null ? buildDefaultCsvTaskHeader(exportTasks) : config.getDataToExport()
        OutputStream outStream = new FileOutputStream(outFile, false)
        PrintWriter writer = new PrintWriter(outStream, true)
        writer.println(csvHeader.join(","))
        exportTasks.forEach { exportTask ->
            Case taskCase = workflowService.findOne(exportTask.caseId)
            writer.println(buildRecord(csvHeader, taskCase).join(",").replace("\n", "\\n"))
        }
        writer.close()
        return outStream
    }

    private List<String> buildRecord(Set<String> csvHeader, Case exportCase) {
        StringEscapeUtils escapeUtils = new StringEscapeUtils()
        List<String> record = new LinkedList<>()
        csvHeader.forEach { dataFieldId ->
            if (exportCase.dataSet.containsKey(dataFieldId)) {
                record.add(escapeUtils.escapeCsv(resolveFieldValue(exportCase, dataFieldId)))
            } else record.add("")
        }
        return record
    }

    private String resolveFieldValue(Case exportCase, String exportFieldId) {
        String fieldValue
        Field field = exportCase.getField(exportFieldId)
        if (field.value == null && exportCase.dataSet[exportFieldId].value == null) {
            return ""
        }
        switch (field.type) {
            case FieldType.MULTICHOICE_MAP:
                fieldValue = (field as MultichoiceMapField).value.stream()
                        .filter { value -> field.options.keySet().contains(value.trim()) }
                        .map { value -> field.options.get(value.trim()).defaultValue }
                        .collect(Collectors.toList()).join(";")
                break
            case FieldType.ENUMERATION_MAP:
                fieldValue = (field as EnumerationMapField).options.get(field.value).defaultValue
                break
            case FieldType.MULTICHOICE:
                fieldValue = (field as MultichoiceField).value.join(";")
                break
            case FieldType.FILE:
                fieldValue = field.value
                break
            case FieldType.FILELIST:
                fieldValue = field.value.namesPaths.join(";")
                break
            case FieldType.TASK_REF:
                fieldValue = field.value.join(";")
                break
            case FieldType.USER:
                fieldValue = field.value.email
                break
            case FieldType.USERLIST:
                fieldValue = field.value
                break
            default:
                fieldValue = field.value == null ? exportCase.dataSet[exportFieldId].value as String : field.value as String
                break
        }
        return fieldValue
    }
}
