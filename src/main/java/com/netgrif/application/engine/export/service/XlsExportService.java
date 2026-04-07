package com.netgrif.application.engine.export.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.export.configuration.XlsExportProperties;
import com.netgrif.application.engine.export.service.interfaces.IXlsExportService;
import com.netgrif.application.engine.export.domain.CellFactory;
import com.netgrif.application.engine.export.domain.ExportedField;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.export.web.requestbodies.FilteredCasesRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class XlsExportService implements IXlsExportService {

    private final IWorkflowService workflowService;
    private final IElasticIndexService elasticIndexService;
    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final IElasticCaseService elasticCaseService;
    private final IPetriNetService petriNetService;
    private final XlsExportProperties exportProperties;

    @PostConstruct
    public void init() {
        if (exportProperties.getDatePattern() != null && !exportProperties.getDatePattern().isBlank()) {
            CellFactory.DATE_PATTERN = exportProperties.getDatePattern();
        }
        if (exportProperties.getDateTimePattern() != null && !exportProperties.getDateTimePattern().isBlank()) {
            CellFactory.DATE_TIME_PATTERN = exportProperties.getDateTimePattern();
        }
    }

    @Override
    public File getExportFilteredCasesFile(FilteredCasesRequest request, LoggedUser user, Locale locale) throws Exception {
        List<ExportedField> fieldsToExport = ExportedField.convert(request.getSelectedDataFieldIds(), request.getSelectedDataFieldNames());
        fieldsToExport = insertPredefinedFields(fieldsToExport, getProcessIdentifierFromFilteredRequest(request));
        return getCasesToExcel(request.getQuery(), fieldsToExport, user, locale, request.getIsIntersection());
    }

    @Override
    public File getExportFilteredCasesFile(List<CaseSearchRequest> requests, Boolean isIntersection, List<ExportedField> selectedField, LoggedUser user, Locale locale) throws Exception {
        return getCasesToExcel(requests, insertPredefinedFields(selectedField), user, locale, isIntersection);
    }

    protected List<ExportedField> insertPredefinedFields(List<ExportedField> fieldToExport) {
        return insertPredefinedFields(fieldToExport, null);
    }

    protected List<ExportedField> insertPredefinedFields(List<ExportedField> fieldToExport, String processIdentifier) {
        if (fieldToExport == null) return new ArrayList<>();
        Set<ExportedField> fields = new LinkedHashSet<>(fieldToExport);
        if (exportProperties.isAddMetaData()) {
            replaceInSet(fields, ExportedField.STRING_ID);
            replaceInSet(fields, ExportedField.VISUAL_ID);
            replaceInSet(fields, ExportedField.AUTHOR);
            replaceInSet(fields, ExportedField.TITLE);
            replaceInSet(fields, ExportedField.CREATION_DATE);
        }

        if (!exportProperties.isExportAllImmediateFields()) {
            return new ArrayList<>(fields);
        }

        if (processIdentifier == null || processIdentifier.isBlank()) {
            return new ArrayList<>(fields);
        }

        PetriNet process = petriNetService.getNewestVersionByIdentifier(processIdentifier);
        process.getImmediateFields().stream()
                .filter(f -> !f.getName().getDefaultValue().isBlank())
                .map(f -> new ExportedField(f.getImportId(), f.getName().getDefaultValue()))
                .forEachOrdered(fields::add);
        return new ArrayList<>(fields);
    }

    protected <T> void replaceInSet(Set<T> fields, T field) {
        boolean added = fields.add(field);
        if (!added) {
            fields.remove(field);
            fields.add(field);
        }
    }

    private File getCasesToExcel(List<CaseSearchRequest> requests, List<ExportedField> fields, LoggedUser user, Locale locale, Boolean isIntersection) throws Exception {
        log.info("Exporting cases to xlsx file. Query: {}", requests.stream().map(request -> request.query).collect(Collectors.joining(", ")));
        long caseCount = elasticCaseService.count(requests, user, locale, isIntersection);
        boolean isResultTrimmed = false;
        if (exportProperties.getMaxRows() > 0) {
            if (caseCount > exportProperties.getMaxRows()) {
                log.warn("Requested case export could resulted in {} rows. Trimming result to {} row as configured in nae.xls.export.max-rows", caseCount, exportProperties.getMaxRows());
                isResultTrimmed = true;
                caseCount = exportProperties.getMaxRows();
            }
        }
        long numberOfPagesNeeded = caseCount % exportProperties.getPageSize() == 0 ? (caseCount / exportProperties.getPageSize()) : (caseCount / exportProperties.getPageSize()) + 1;

        SXSSFWorkbook workbook = new SXSSFWorkbook(exportProperties.getPageSize()); // https://poi.apache.org/components/spreadsheet/how-to.html#sxssf
        Sheet sheet = workbook.createSheet(exportProperties.getSheetName());
        insertHeader(fields, sheet);

        int sizeOfProcessedRows = 0;
        int counter = 0;
        List<String> scrollIdsToClear = new ArrayList<>();
        NativeSearchQuery query = elasticCaseService.buildQuery(requests, user, PageRequest.of(0, exportProperties.getPageSize()), Locale.ENGLISH, true);
        SearchScrollHits<?> scroll = elasticIndexService.scrollFirst(query, ElasticCase.class);
        while (scroll.hasSearchHits() && counter < numberOfPagesNeeded) {
            Page<ElasticCase> indexedCases = (Page) SearchHitSupport.unwrapSearchHits(SearchHitSupport.searchPageFor(scroll, query.getPageable()));
            Page<Case> page = new PageImpl<>(workflowService.findAllById(indexedCases.get().map(ElasticCase::getStringId).collect(Collectors.toList())), query.getPageable(), scroll.getTotalHits());
            scrollIdsToClear.add(scroll.getScrollId());
            sizeOfProcessedRows = processPage(page, sheet, fields, sizeOfProcessedRows);
            counter += 1;
            scroll = elasticIndexService.scroll(scroll.getScrollId(), ElasticCase.class);
        }
        scrollIdsToClear.add(scroll.getScrollId());
        elasticsearchTemplate.searchScrollClear(scrollIdsToClear);

        if (isResultTrimmed) {
            int lasRow = sheet.getLastRowNum() < 0 ? 0 : sheet.getLastRowNum() + 1;
            sheet.createRow(lasRow).createCell(0);
            sheet.createRow(lasRow + 1).createCell(0).setCellValue(exportProperties.getTrimWarningMessage());
        }

        File result = File.createTempFile("case-export-" + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), ".xlsx");
        try (FileOutputStream fout = new FileOutputStream(result)) {
            workbook.write(fout);
        } catch (Exception ex) {
            log.error("Cannot create export for provided query", ex);
            throw ex;
        } finally {
            workbook.close();
            workbook.dispose();
        }
        log.info("Successfully exported {} cases to xlsx file.", caseCount);
        return result;
    }

    private void insertHeader(List<ExportedField> fields, Sheet sheet) {
        Row header = sheet.createRow(0);
        IntStream.range(0, fields.size()).forEach(idx -> {
            Cell cell = header.createCell(idx);
            cell.setCellValue(fields.get(idx).getName());
        });
    }

    private int processPage(Page<Case> page, Sheet sheet, List<ExportedField> fieldsToExport, int numberOfProcessedItems) {
        int rowIndex = Math.toIntExact(numberOfProcessedItems == 0 ? ((long) page.getNumber() * exportProperties.getPageSize()) + 1 : numberOfProcessedItems);
        for (Case caze : page.getContent()) {
            Row row = processCase(caze, sheet, rowIndex, fieldsToExport);
            if (row != null)
                rowIndex++;
        }
        return rowIndex;
    }

    private Row processCase(Case caze, Sheet sheet, int rowIndex, List<ExportedField> fieldsToExport) {
        rowIndex = Math.max(rowIndex, 0);
        Row row = sheet.createRow(rowIndex);
        fieldsToExport.forEach(field -> processField(caze, field, row));
        return row;
    }

    private Cell processField(Case caze, ExportedField fieldToExport, Row row) {
        Object value = resolveFieldValue(caze, fieldToExport);
        FieldType fieldType = caze.getField(fieldToExport.getId()) == null ? FieldType.TEXT : caze.getField(fieldToExport.getId()).getType();
        int cellNum = row.getLastCellNum() < 0 ? 0 : row.getLastCellNum();
        return CellFactory.create(row, cellNum, fieldType, value);
    }

    private Object resolveFieldValue(Case caze, ExportedField field) {
        try {
            if (field.isMeta()) {
                return resolveMetaFieldValue(caze, field);
            }
            if (caze.getField(field.getId()).getType() == FieldType.ENUMERATION_MAP) {
                Map<?, ?> options = caze.getDataField(field.getId()).getOptions();
                if (options == null || options.isEmpty()) {
                    options = ((MapOptionsField<?, ?>) caze.getField(field.getId())).getOptions();
                }
                Object value = caze.getFieldValue(field.getId());
                return value == null ? null : options.get(value.toString()).toString();
            }
            return caze.getFieldValue(field.getId());
        } catch (Exception ex) {
            return "ERROR";
        }
    }

    private Object resolveMetaFieldValue(Case caze, ExportedField field) {
        if (!field.isMeta()) return null;
        if (field.getId().contains("title"))
            return caze.getTitle();
        if (field.getId().contains("author"))
            return caze.getAuthor().getFullName();
        if (field.getId().contains("creationDate"))
            return caze.getCreationDate().format(DateTimeFormatter.ofPattern(exportProperties.getDateTimePattern()));
        if (field.getId().contains("visualId"))
            return caze.getVisualId();
        if (field.getId().contains("stringId"))
            return caze.getStringId();
        return null;
    }

    private String getProcessIdentifierFromFilteredRequest(FilteredCasesRequest request) {
        if (request.getQuery() == null ||
                request.getQuery().isEmpty() ||
                request.getQuery().get(0) == null ||
                request.getQuery().get(0).query == null ||
                request.getQuery().get(0).query.isBlank()) {
            return "";
        }
        return Arrays.stream(request.getQuery().get(0).query.split("\\s+"))
                .filter(part -> part.startsWith("processIdentifier:"))
                .map(part -> part.split(":", 2)[1])
                .findFirst()
                .orElse("");
    }
}
