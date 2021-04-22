package com.netgrif.workflow.export.service

import com.netgrif.workflow.export.domain.ExportDataConfig
import com.netgrif.workflow.export.service.interfaces.IExportHelperService
import com.netgrif.workflow.petrinet.domain.dataset.*
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringEscapeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.stream.Collectors

@Service
@Slf4j
class ExportHelperService implements IExportHelperService{

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Override
    Set<String> buildDefaultCsvCaseHeader(List<Case> exportCases) {
        Set<String> header = new LinkedHashSet<>()
        exportCases.forEach{exportCase ->
            header.addAll(exportCase.immediateDataFields)
        }
        return header
    }

    @Override
    Set<String> buildDefaultCsvTaskHeader(List<Task> exportTasks) {
        Set<String> header = new LinkedHashSet<>()
        exportTasks.forEach{exportTask ->
            header.addAll(exportTask.immediateDataFields)
        }
        return header
    }

    @Override
    OutputStream fillCsvCaseData(File outFile, List<Case> exportCases, ExportDataConfig config) {
        Set<String> csvHeader = config == null ? buildDefaultCsvCaseHeader(exportCases) : config.getDataToExport()
        OutputStream outStream = new FileOutputStream(outFile,false)
        PrintWriter writer = new PrintWriter(outStream,true)
        writer.println(csvHeader.join(","))
        exportCases.forEach { exportCase ->
            writer.println(buildRecord(csvHeader, exportCase).join(",").replace("\n", "\\n"))
        }
        writer.close()
        return outStream
    }

    @Override
    OutputStream fillCsvTaskData(File outFile, List<Task> exportTasks, ExportDataConfig config) {
        Set<String> csvHeader = config == null ? buildDefaultCsvTaskHeader(exportTasks) : config.getDataToExport()
        OutputStream outStream = new FileOutputStream(outFile,false)
        PrintWriter writer = new PrintWriter(outStream,true)
        writer.println(csvHeader.join(","))
        exportTasks.forEach{ exportTask ->
            Case taskCase = workflowService.findOne(exportTask.caseId)
            writer.println(buildRecord(csvHeader, taskCase).join(",").replace("\n", "\\n"))
        }
        writer.close()
        return outStream
    }

    private List<String> buildRecord(Set<String> csvHeader, Case exportCase){
        StringEscapeUtils escapeUtils = new StringEscapeUtils()
        List<String> record = new LinkedList<>()
        csvHeader.forEach{dataFieldId ->
            if(exportCase.dataSet.containsKey(dataFieldId)){
                record.add(escapeUtils.escapeCsv(resolveFieldValue(exportCase.getField(dataFieldId))))
            } else record.add("")
        }
        return record
    }

    private String resolveFieldValue(Field field){
        String fieldValue
        if(field.value == null) return ""
        switch (field.type){
            case FieldType.MULTICHOICE_MAP:
                fieldValue = (field as MultichoiceMapField).value.stream()
                        .filter{value -> field.options.keySet().contains(value.trim())}
                        .map{value -> field.options.get(value.trim()).defaultValue}
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
                fieldValue = field.value as String
                break
            }
        return fieldValue
    }
}
