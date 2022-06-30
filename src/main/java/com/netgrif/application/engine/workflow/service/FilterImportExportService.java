package com.netgrif.application.engine.workflow.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.common.collect.Lists;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.configuration.properties.FilterProperties;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.workflow.domain.filter.FilterImportExport;
import com.netgrif.application.engine.workflow.domain.filter.FilterImportExportList;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.DefaultFiltersRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.utils.InputStreamToString;
import com.netgrif.application.engine.workflow.domain.*;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IFilterImportExportService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.*;

/**
 * Service that provides methods for creation of filter export/import cases for users.
 * Also provides methods to export and import filters.
 */

@Service
public class FilterImportExportService implements IFilterImportExportService {

    private static final Logger log = LoggerFactory.getLogger(FilterImportExportService.class);

    private static final String EXPORT_NET_IDENTIFIER = "export_filters";
    private static final String IMPORT_NET_IDENTIFIER = "import_filters";
    private static final String FILTER_NET_IDENTIFIER = "filter";

    private static final String UPLOAD_FILE_FIELD = "upload_file";

    private static final String IMPORT_FILTER_TRANSITION = "import_filter";

    private static final String FIELD_VISIBILITY = "visibility";
    private static final String FIELD_FILTER_TYPE = "filter_type";
    private static final String FIELD_FILTER = "filter";
    private static final String FIELD_NAME = "i18n_filter_name";
    private static final String FIELD_PARENT_CASE_ID = "parent_filter_id";
    private static final String FIELD_PARENT_VIEW_ID = "origin_view_id";
    private static final String FIELD_MISSING_ALLOWED_NETS = "missing_allowed_nets";
    private static final String FIELD_MISSING_NETS_TRANSLATION = "missing_nets_translation";

    @Autowired
    IUserService userService;

    @Autowired
    IWorkflowService workflowService;

    @Autowired
    IPetriNetService petriNetService;

    @Autowired
    DefaultFiltersRunner defaultFiltersRunner;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private FileStorageConfiguration fileStorageConfiguration;

    @Autowired
    private FilterProperties filterProperties;

    @Override
    public void createFilterImport(IUser author) {
        workflowService.createCaseByIdentifier(IMPORT_NET_IDENTIFIER,"Import filters " + author.getFullName(), "", author.transformToLoggedUser());
    }

    @Override
    public void createFilterExport(IUser author) {
        workflowService.createCaseByIdentifier(EXPORT_NET_IDENTIFIER, "Export filters " + author.getFullName(), "", author.transformToLoggedUser());
    }

    /**
     * Method which performs export of selected filters into xml file.
     * Method finds all cases by provided ids, transform them into FilterImportExportList object
     * and serialize them into xml file on path: storage/filterExport/<userId>/filters.xml
     * @param filtersToExport - set of ids of filter cases, which should be exported
     * @return FileFieldValue - file field value with path to xml file of exported filters
     * @throws IOException - if file which contains exported filters cannot be created
     */
    @Override
    public FileFieldValue exportFiltersToFile(Collection<String> filtersToExport) throws IOException {
        log.info("Exporting selected filters");
        return createXML(exportFilters(filtersToExport));
    }

    /**
     * Method which performs export of selected filters into xml file.
     * Method finds all cases by provided ids, transform them into FilterImportExportList object
     * @param filtersToExport - set of ids of filter cases, which should be exported
     * @return a serializable wrapper of a list of filter objects in serializable form
     */
    @Override
    public FilterImportExportList exportFilters(Collection<String> filtersToExport) {
        List<Case> selectedFilterCases = this.workflowService.findAllById(Lists.newArrayList(filtersToExport));
        FilterImportExportList filterList = new FilterImportExportList();

        Set<String> exportedFilterIds = new HashSet<>();

        for (Case exportedFilter : selectedFilterCases) {
            LinkedList<FilterImportExport> chain = new LinkedList<>();
            Case currentCase = exportedFilter;
            while (true) {
                if (exportedFilterIds.contains(currentCase.getStringId())) {
                    break;
                }
                exportedFilterIds.add(currentCase.getStringId());
                FilterImportExport currentFilter = createExportClass(currentCase);
                chain.push(currentFilter);
                if (currentFilter.getParentCaseId() != null) {
                    currentCase = this.workflowService.findOne(currentFilter.getParentCaseId());
                } else {
                    break;
                }
            }
            filterList.getFilters().addAll(chain);
        }

        return filterList;
    }

    /**
     * Method which performs import of filters from uploaded xml file.
     * Method firstly loads xml file from file field and validates it against xml schema for filters
     * export located on path: filter_export_schema.xml
     * If the file is correct, method calls performImport method which
     * creates filter cases
     * @return List<String> - list of task ids of imported filter cases in - import_filter transition
     * @throws IOException - if imported file is not found
     * @throws IllegalFilterFileException - if uploaded xml is not in correct xml format and invalidate against schema
     */
    @Override
    public List<String> importFilters() throws IOException, IllegalFilterFileException, TransitionNotExecutableException {
        log.info("Importing filters");
        FilterImportExportList filterList = loadFromXML();
        return new ArrayList<>(performImport(filterList).values());
    }

    /**
     * Method which performs import of filters from already created filter import class instances
     * passed in as parameter.
     * @param filterList - instance of class FilterImportExportList
     * @return a mapping of original filter case ids to task ids of imported filter cases in - import_filter transition
     * @throws IOException - if imported file is not found
     */
    @Override
    public Map<String, String> importFilters (FilterImportExportList filterList) throws IOException, TransitionNotExecutableException {
        log.info("Importing filters from imported menu");
        return performImport(filterList);
    }

    protected Map<String, String> performImport (FilterImportExportList filterList) throws IOException, TransitionNotExecutableException {
        Map<String, String> oldToNewFilterId = new HashMap<>();
        Map<String, String> importedFilterTaskIds = new HashMap<>();

        if (filterList == null) {
            throw new FileNotFoundException();
        }

        filterList.getFilters().forEach(filter -> {
            if (filter.getAllowedNets() == null) {
                filter.setAllowedNets(new ArrayList<>());
            }

            String parentId = null;
            boolean viewOrigin = false;

            if (filter.getParentCaseId() != null && !filter.getParentCaseId().equals("")) {
                parentId = oldToNewFilterId.get(filter.getParentCaseId());
                if (parentId == null) {
                    log.error("Imported filter with ID '" + filter.getCaseId() + "' could not find an imported mapping of its parent case with original ID '" + filter.getParentCaseId() + "'");
                }
            } else if (filter.getParentViewId() != null && !filter.getParentViewId().equals("")) {
                parentId = filter.getParentViewId();
                viewOrigin = true;
            }

            Optional<Case> filterCase = defaultFiltersRunner.createFilter(
                    filter.getFilterName().getDefaultValue(),
                    filter.getIcon(),
                    filter.getType(),
                    filter.getVisibility(),
                    filter.getFilterValue(),
                    filter.getAllowedNets(),
                    filter.getFilterMetadataExport().getMapObject(),
                    filter.getFilterName().getTranslations(),
                    filter.getFilterMetadataExport().isDefaultSearchCategories(),
                    filter.getFilterMetadataExport().isInheritAllowedNets(),
                    parentId,
                    viewOrigin,
                    true
            );

            if (filterCase.isEmpty()) {
                return;
            }

            oldToNewFilterId.put(filter.getCaseId(), filterCase.get().getStringId());

            Task importedFilterTask = taskService.searchOne(
                    QTask.task.transitionId.eq(IMPORT_FILTER_TRANSITION)
                            .and(QTask.task.caseId.eq(filterCase.get().getStringId()))
            );
            importedFilterTaskIds.put(filter.getCaseId(), importedFilterTask.getStringId());

            // TODO: delete after fixed issue: https://netgrif.atlassian.net/jira/servicedesk/projects/NGSD/issues/
            filterCase.get().getDataSet().get(FIELD_MISSING_ALLOWED_NETS).addBehavior(IMPORT_FILTER_TRANSITION, Collections.singleton(FieldBehavior.HIDDEN));
            filterCase.get().getDataSet().get(FIELD_FILTER).addBehavior(IMPORT_FILTER_TRANSITION, Collections.singleton(FieldBehavior.VISIBLE));
            workflowService.save(filterCase.get());
        });
        taskService.assignTasks(taskService.findAllById(new ArrayList<>(importedFilterTaskIds.values())), userService.getLoggedUser());
        changeFilterField(importedFilterTaskIds.values());
        return importedFilterTaskIds;
    }

    /**
     * Method which provides reloading of imported filters fields, so if allowed nets are missing,
     * htmlTextArea is shown with list of missing allowed nets, otherwise filter preview is shown.
     * @param filterFields - list of task ids of filters which value should be reloaded
     */
    @Override
    public void changeFilterField(Collection<String> filterFields) {
        filterFields.forEach(f -> {
            Task importedFilterTask = taskService.findOne(f);
            Case filterCase = workflowService.findOne(importedFilterTask.getCaseId());
            PetriNet filterNet = petriNetService.getNewestVersionByIdentifier(FILTER_NET_IDENTIFIER);
            List<String> requiredNets = filterCase.getDataSet().get(FIELD_FILTER).getAllowedNets();
            List<String> currentNets = petriNetService.getExistingPetriNetIdentifiersFromIdentifiersList(requiredNets);
            if (currentNets.size() < requiredNets.size()) {
                requiredNets.removeAll(currentNets);
                StringBuilder htmlTextAreaValue = new StringBuilder(
                        ((EnumerationMapField) filterNet.getDataSet().get(FIELD_MISSING_NETS_TRANSLATION)).getOptions().get(
                                LocaleContextHolder.getLocale().getLanguage()
                        ).getDefaultValue()
                );
                htmlTextAreaValue.append("<ul style=\"color: red\">");
                requiredNets.forEach(net -> htmlTextAreaValue.append("<li>").append(net).append("</li>"));
                htmlTextAreaValue.append("</ul>");
                Map<String, Map<String, String>> taskData = new HashMap<>();
                Map<String, String> missingNets = new HashMap<>();
                missingNets.put("type", "text");
                missingNets.put("value", htmlTextAreaValue.toString());
                taskData.put(FIELD_MISSING_ALLOWED_NETS, missingNets);
                this.dataService.setData(importedFilterTask, ImportHelper.populateDataset(taskData));
                filterCase = workflowService.findOne(filterCase.getStringId());
                changeVisibilityByAllowedNets(true, filterCase);
            } else {
                changeVisibilityByAllowedNets(false, filterCase);
            }
            workflowService.save(filterCase);
        });
    }

    private void changeVisibilityByAllowedNets(boolean allowedNetsMissing, Case filterCase) {
        filterCase.getDataSet().get(allowedNetsMissing ? FIELD_MISSING_ALLOWED_NETS : FIELD_FILTER).makeVisible(IMPORT_FILTER_TRANSITION);
        filterCase.getDataSet().get(allowedNetsMissing ? FIELD_FILTER : FIELD_MISSING_ALLOWED_NETS).makeHidden(IMPORT_FILTER_TRANSITION);
    }

    @Transactional
    protected FilterImportExportList loadFromXML() throws IOException, IllegalFilterFileException {
        Case exportCase = workflowService.searchOne(
                QCase.case$.processIdentifier.eq(IMPORT_NET_IDENTIFIER)
                        .and(QCase.case$.author.id.eq(userService.getLoggedUser().getStringId()))
        );

        FileFieldValue ffv = (FileFieldValue) exportCase.getDataSet().get(UPLOAD_FILE_FIELD).getValue();
        if (ffv == null) {
            throw new FileNotFoundException();
        }

        File f = new File(ffv.getPath());
        validateFilterXML(new FileInputStream(f));
        String importedFilter = InputStreamToString.inputStreamToString(new FileInputStream(f));
        SimpleModule module = new SimpleModule().addDeserializer(Object.class, FilterDeserializer.getInstance());
        XmlMapper xmlMapper = (XmlMapper) new XmlMapper().registerModule(module);
        return xmlMapper.readValue(importedFilter, FilterImportExportList.class);
    }

    @Transactional
    protected FileFieldValue createXML(FilterImportExportList filters) throws IOException {
        String filePath = fileStorageConfiguration.getStoragePath() + "/filterExport/" + userService.getLoggedUser().getStringId() + "/" + filterProperties.getFileName();
        File f = new File(filePath);
        f.getParentFile().mkdirs();

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xmlMapper.writeValue(baos, filters);

        FileOutputStream fos = new FileOutputStream(f);
        baos.writeTo(fos);

        return new FileFieldValue(filterProperties.getFileName(), filePath);
    }

    protected FilterImportExport createExportClass(Case filter) {
        FilterImportExport exportFilter = new FilterImportExport();
        exportFilter.setCaseId(filter.getStringId());
        exportFilter.setIcon(filter.getIcon());

        DataField parentCaseId = filter.getDataField(FIELD_PARENT_CASE_ID);
        if (parentCaseId.getValue() != null && !parentCaseId.getValue().equals("")) {
            exportFilter.setParentCaseId((String) parentCaseId.getValue());
        }

        DataField parentViewId = filter.getDataField(FIELD_PARENT_VIEW_ID);
        if (parentViewId.getValue() != null && !parentViewId.getValue().equals("")) {
            exportFilter.setParentViewId((String) parentViewId.getValue());
        }

        DataField filterField = filter.getDataField(FIELD_FILTER);
        exportFilter.setFilterValue((String) filterField.getValue());
        exportFilter.setAllowedNets(filterField.getAllowedNets());
        exportFilter.setFilterMetadataExport(filterField.getFilterMetadata());

        DataField visibility = filter.getDataField(FIELD_VISIBILITY);
        exportFilter.setVisibility(visibility.getValue().toString());

        DataField type = filter.getDataField(FIELD_FILTER_TYPE);
        exportFilter.setType(type.getValue().toString());

        DataField name = filter.getDataField(FIELD_NAME);
        exportFilter.setFilterName((I18nString) name.getValue());

        return exportFilter;
    }

    private static void validateFilterXML(InputStream xml) throws IllegalFilterFileException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(FilterImportExportService.class.getResource("/petriNets/filter_export_schema.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
        } catch (Exception ex) {
            throw new IllegalFilterFileException(ex);
        }
    }
}



