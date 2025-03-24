package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.importer.model.*;
import com.netgrif.application.engine.importer.model.Properties;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.workflow.domain.triggers.AutoTrigger;
import com.netgrif.application.engine.workflow.domain.triggers.TimeTrigger;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.OutputStream;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
public class CaseExporter {

    @Autowired
    private ITaskService taskService;

    @Value("${nae.schema.location}")
    private String schemaLocation;

    private final ObjectFactory objectFactory = new ObjectFactory();

    private OutputStream outputStream;
    private com.netgrif.application.engine.workflow.domain.Case caseToExport;
    private Case xmlCase;
    private HashMap<String, I18N> translations;

    //    todo custom error handling?
    public void exportCases(Collection<com.netgrif.application.engine.workflow.domain.Case> casesToExport, OutputStream outputStream) throws RuntimeException {
        this.outputStream = outputStream;

        Cases xmlCases = objectFactory.createCases();
        casesToExport.forEach(caseToExport -> {
            this.caseToExport = caseToExport;
            this.translations = new HashMap<>();
            xmlCases.getCase().add(exportCase());
        });

        try {
            marshallCase(xmlCases);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    protected void marshallCase(com.netgrif.application.engine.importer.model.Cases caseToExport) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(com.netgrif.application.engine.importer.model.Cases.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, schemaLocation);
        marshaller.marshal(caseToExport, this.outputStream);
    }

    private Case exportCase() {
        this.xmlCase = objectFactory.createCase();
        exportCaseMetadata(caseToExport);
        exportTasks();
        exportDataFields();
        this.xmlCase.getI18N().addAll(this.translations.values());
        return xmlCase;
    }

    private void exportCaseMetadata(com.netgrif.application.engine.workflow.domain.Case caseToExport) {
        this.xmlCase.setId(caseToExport.getStringId());
//        todo should the whole object be exported? is id specific enough? should email be exported instead of id?
        this.xmlCase.setAuthor(caseToExport.getAuthor().getId());
        this.xmlCase.setColor(caseToExport.getColor());
        this.xmlCase.setProcessVersion(caseToExport.getPetriNet().getVersion().toString());
        this.xmlCase.setProcessIdentifier(caseToExport.getProcessIdentifier());
        this.xmlCase.setVisualId(caseToExport.getVisualId());
        this.xmlCase.setUriNodeId(caseToExport.getUriNodeId());
        this.xmlCase.setTags(exportTags(caseToExport.getTags()));
        this.xmlCase.setTitle(caseToExport.getTitle());
        this.xmlCase.setCreationDate(exportLocalDateTime(caseToExport.getCreationDate()));
        this.xmlCase.setLastModified(exportLocalDateTime(caseToExport.getLastModified()));
        this.xmlCase.setViewRoles(exportCollectionOfStrings(caseToExport.getViewRoles()));
        this.xmlCase.setViewUserRefs(exportCollectionOfStrings(caseToExport.getNegativeViewUsers()));
        this.xmlCase.setViewUsers(exportCollectionOfStrings(caseToExport.getNegativeViewUsers()));
        this.xmlCase.setNegativeViewUsers(exportCollectionOfStrings(caseToExport.getNegativeViewUsers()));
        this.xmlCase.setActivePlaces(exportMapXsdType(caseToExport.getActivePlaces()));
        this.xmlCase.setConsumedTokens(exportMapXsdType(caseToExport.getConsumedTokens()));
        this.xmlCase.setUsers(exportPermissions(caseToExport.getUsers()));
    }

    private void exportTasks() {
        List<com.netgrif.application.engine.workflow.domain.Task> tasksToExport = caseToExport.getTasks().stream()
                .map(taskPair -> taskService.findOne(taskPair.getTask()))
                .toList();
        tasksToExport.forEach(taskToExport -> this.xmlCase.getTask().add(exportTask(taskToExport)));
    }

    private Task exportTask(com.netgrif.application.engine.workflow.domain.Task taskToExport) {
        Task xmlTask = objectFactory.createTask();
        xmlTask.setId(taskToExport.getStringId());
        xmlTask.setTransitionId(taskToExport.getTransitionId());
        xmlTask.setTitle(exportI18NString(taskToExport.getTitle()));
        xmlTask.setPriority(exportInteger(taskToExport.getPriority()));
        xmlTask.setUserId(taskToExport.getUserId());
        xmlTask.setStartDate(exportLocalDateTime(taskToExport.getStartDate()));
        xmlTask.setFinishDate(exportLocalDateTime(taskToExport.getFinishDate()));
        xmlTask.setFinishedBy(taskToExport.getFinishedBy());
        xmlTask.setTransactionId(taskToExport.getTransactionId());
        xmlTask.setIcon(taskToExport.getIcon());
        xmlTask.setAssignPolicy(AssignPolicy.fromValue(taskToExport.getAssignPolicy().toString().toLowerCase()));
        xmlTask.setDataFocusPolicy(DataFocusPolicy.fromValue(taskToExport.getDataFocusPolicy().toString().toLowerCase()));
        xmlTask.setFinishPolicy(FinishPolicy.fromValue(taskToExport.getFinishPolicy().toString().toLowerCase()));
        xmlTask.setTags(exportTags(taskToExport.getTags()));
        xmlTask.setViewRoles(exportCollectionOfStrings(taskToExport.getViewRoles()));
        xmlTask.setViewUserRefs(exportCollectionOfStrings(taskToExport.getNegativeViewUsers()));
        xmlTask.setViewUsers(exportCollectionOfStrings(taskToExport.getNegativeViewUsers()));
        xmlTask.setNegativeViewRoles(exportCollectionOfStrings(taskToExport.getNegativeViewRoles()));
        xmlTask.setNegativeViewUsers(exportCollectionOfStrings(taskToExport.getNegativeViewUsers()));
        xmlTask.setImmediateDataFields(exportCollectionOfStrings(taskToExport.getImmediateDataFields()));
        xmlTask.setUserRef(exportPermissions(taskToExport.getUserRefs()));
        xmlTask.setUserRef(exportPermissions(taskToExport.getUserRefs()));
        xmlTask.setUser(exportPermissions(taskToExport.getUsers()));
        xmlTask.setAssignedUserPolicies(exportAssignedUserPolicy(taskToExport.getAssignedUserPolicy()));
        xmlTask.setTriggers(exportTriggers(taskToExport.getTriggers()));
        return xmlTask;
    }

    private void exportDataFields() {
        LinkedHashMap<String, com.netgrif.application.engine.workflow.domain.DataField> dataSet = this.caseToExport.getDataSet();
        if (dataSet == null || dataSet.isEmpty()) {
            return;
        }
        dataSet.forEach((key, value) -> xmlCase.getDataField().add(exportDataField(key, value)));
    }

    private DataField exportDataField(String fieldId, com.netgrif.application.engine.workflow.domain.DataField dataFieldToExport) {
        DataField xmlDataField = objectFactory.createDataField();
        xmlDataField.setId(fieldId);
        xmlDataField.setType(DataType.fromValue(caseToExport.getField(fieldId).getType().getName()));
        xmlDataField.setValues(exportDataFieldValue(dataFieldToExport.getValue(), caseToExport.getField(fieldId).getType()));
        if (this.caseToExport.getField(fieldId).getType().equals(FieldType.FILTER)) {
            xmlDataField.setFilterMetadata(exportFilterMetadata(dataFieldToExport.getFilterMetadata()));
        }
        xmlDataField.setEncryption(dataFieldToExport.getEncryption());
        xmlDataField.setLastModified(exportLocalDateTime(dataFieldToExport.getLastModified()));
        xmlDataField.setVersion(dataFieldToExport.getVersion());
        xmlDataField.setAllowedNets(exportCollectionOfStrings(dataFieldToExport.getAllowedNets()));
        xmlDataField.setComponent(exportComponent(dataFieldToExport.getComponent()));
        xmlDataField.getDataRefComponent().addAll(exportDataRefComponents(dataFieldToExport.getDataRefComponents()));
        xmlDataField.setValidations(exportValidations(dataFieldToExport.getValidations()));
        xmlDataField.setOptions(exportOptions(dataFieldToExport.getOptions()));
        xmlDataField.setBehaviors(exportTaskBehaviors(dataFieldToExport.getBehavior()));
        return xmlDataField;
    }

    private FilterMetadata exportFilterMetadata(Map<String, Object> filterMetadata) {
//        todo refactor whole metadata object
        if (filterMetadata == null || filterMetadata.isEmpty()) {
            return null;
        }
        FilterMetadata xmlMetadata = objectFactory.createFilterMetadata();
        xmlMetadata.setFilterType(FilterType.fromValue(filterMetadata.get("filterType").toString()));
        xmlMetadata.setPredicateMetadata(parsePredicateTreeMetadata((List<List<Object>>) filterMetadata.get("predicateTreeMetadata")));
        xmlMetadata.getSearchCategories().getValue().addAll((List<String>) filterMetadata.get("searchCategories"));
        xmlMetadata.setDefaultSearchCategories((Boolean) filterMetadata.get("defaultSearchCategories"));
        xmlMetadata.setInheritAllowedNets((Boolean) filterMetadata.get("inheritAllowedNets"));
        return xmlMetadata;
    }

    private PredicateTreeMetadata parsePredicateTreeMetadata(List<List<Object>> predicateTreeMetadata) {
        if (predicateTreeMetadata == null || predicateTreeMetadata.isEmpty()) {
            return null;
        }
        PredicateTreeMetadata xmlPredicateTreeMetadata = objectFactory.createPredicateTreeMetadata();
        predicateTreeMetadata.forEach(list -> {
            if (list == null || list.isEmpty()) {
                return;
            }
            PredicateMetadataArray metadataArray = objectFactory.createPredicateMetadataArray();
            list.forEach(data -> {
                if (data == null) {
                    return;
                }
                CategoryGeneratorMetadata metadata = objectFactory.createCategoryGeneratorMetadata();
                Map<String, Object> retypedData = (Map<String, Object>) data;
                metadata.setCategory(retypedData.get("category").toString());
                metadata.setValues(exportCollectionOfStrings((Collection<String>) retypedData.get("category")));
                metadata.setConfiguration(exportMetadataConfiguration((Map<String, String>) retypedData.get("configuration")));
                metadataArray.getData().add(metadata);
            });
            xmlPredicateTreeMetadata.getPredicate().add(metadataArray);
        });
        return xmlPredicateTreeMetadata;
    }

    private CategoryMetadataConfiguration exportMetadataConfiguration(Map<String, String> configuration) {
        if (configuration == null || configuration.isEmpty()) {
            return null;
        }
        CategoryMetadataConfiguration xmlMetadata = objectFactory.createCategoryMetadataConfiguration();
        configuration.forEach((key, value) -> {
            ConfigurationValue xmlValue = objectFactory.createConfigurationValue();
            xmlValue.setValue(value);
            xmlValue.setId(key);
            xmlMetadata.getValue().add(xmlValue);
        });
        return xmlMetadata;
    }

    private TaskBehaviors exportTaskBehaviors(Map<String, Set<FieldBehavior>> taskBehavior) {
        if (taskBehavior == null || taskBehavior.isEmpty()) {
            return null;
        }
        TaskBehaviors xmlTaskBehaviors = new TaskBehaviors();
        taskBehavior.forEach((taskId, behaviors) -> {
            Behaviors xmlBehavior = objectFactory.createBehaviors();
            xmlBehavior.setTaskId(taskId);
            behaviors.forEach(behavior -> xmlBehavior.getBehavior().add(Behavior.fromValue(behavior.toString())));
            xmlTaskBehaviors.getTaskBehavior().add(xmlBehavior);
        });
        return xmlTaskBehaviors;
    }

    private Options exportOptions(Map<String, I18nString> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        Options xmlOptions = objectFactory.createOptions();
        options.forEach((key, option) -> {
            Option xmlOption = objectFactory.createOption();
            xmlOption.setKey(key);
            xmlOption.setName(option.getKey());
            xmlOption.setValue(option.getDefaultValue());
            xmlOptions.getOption().add(xmlOption);
            exportTranslations(option.getTranslations(), option.getKey());
        });
        return xmlOptions;
    }

    private void exportTranslations(Map<String, String> translations, String name) {
        translations.forEach((locale, translation) -> {
            I18N localeTranslations = this.translations.get(locale);
            if (localeTranslations == null) {
                localeTranslations = objectFactory.createI18N();
                localeTranslations.setLocale(locale);
                this.translations.put(locale, localeTranslations);
            }
            I18NStringType i18nStringType = objectFactory.createI18NStringType();
            i18nStringType.setValue(translation);
            i18nStringType.setName(name);
            localeTranslations.getI18NString().add(i18nStringType);
        });
    }

    private Validations exportValidations(List<com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation> validations) {
        if (validations == null || validations.isEmpty()) {
            return null;
        }
        Validations xmlValidations = objectFactory.createValidations();
        validations.forEach(validation -> {
//            todo resolve other Validation object properties after NAE-1892 is merged into 7.0.0
            Validation xmlValidation = objectFactory.createValidation();
            xmlValidation.setMessage(exportI18NString(validation.getValidationMessage()));
            xmlValidations.getValidation().add(xmlValidation);
        });
        return xmlValidations;
    }

    private List<DataRefComponent> exportDataRefComponents(Map<String, com.netgrif.application.engine.petrinet.domain.Component> dataRefComponents) {
        if (dataRefComponents == null || dataRefComponents.isEmpty()) {
            return Collections.emptyList();
        }
        List<DataRefComponent> xmlDataRefComponents = new ArrayList<>();
        dataRefComponents.forEach((taskId, component) -> {
            DataRefComponent xmlDataRefComponent = objectFactory.createDataRefComponent();
            Component xmlComponent = exportComponent(component);
            xmlDataRefComponent.setTaskId(taskId);
            xmlDataRefComponent.setComponent(xmlComponent);
            xmlDataRefComponents.add(xmlDataRefComponent);
        });
        return xmlDataRefComponents;
    }

    private Component exportComponent(com.netgrif.application.engine.petrinet.domain.Component component) {
        if (component == null) {
            return null;
        }
        Component xmlComponent = objectFactory.createComponent();
        xmlComponent.setName(component.getName());
        xmlComponent.setProperties(exportProperties(component.getProperties(), component.getOptionIcons()));
        return xmlComponent;
    }

    private Properties exportProperties(Map<String, String> properties, List<com.netgrif.application.engine.petrinet.domain.Icon> optionIcons) {
        if ((properties == null || properties.isEmpty()) && (optionIcons == null || optionIcons.isEmpty())) {
            return null;
        }
        Properties xmlProperties = objectFactory.createProperties();
        if (properties != null) {
            properties.forEach((key, value) -> {
                Property xmlProperty = objectFactory.createProperty();
                xmlProperty.setKey(key);
                xmlProperty.setValue(value);
                xmlProperties.getProperty().add(xmlProperty);
            });
        }
        if (optionIcons != null) {
            optionIcons.forEach((icon) -> {
                Icons xmlIcons = objectFactory.createIcons();
                Icon xmlIcon = objectFactory.createIcon();
                xmlIcon.setKey(icon.getKey());
                xmlIcon.setValue(icon.getValue());
                xmlIcon.setType(IconType.fromValue(icon.getType()));
                xmlProperties.setOptionIcons(xmlIcons);
            });
        }
        return xmlProperties;
    }

    private StringCollection exportDataFieldValue(Object value, FieldType type) {
        if (value == null) {
            return null;
        }
        StringCollection values = objectFactory.createStringCollection();
        switch (type) {
            case DATE:
            case DATETIME:
                LocalDateTime localDateTime = value instanceof Date ? convertDateToLocalDateTime((Date) value) : (value instanceof LocalDate ? ((LocalDate) value).atTime(LocalTime.NOON) : (LocalDateTime) value);
                values.getValue().add(exportLocalDateTime(localDateTime));
                break;
            case CASE_REF:
            case TASK_REF:
            case MULTICHOICE:
            case STRING_COLLECTION:
            case MULTICHOICE_MAP:
                ((Collection) value).forEach(it -> values.getValue().add(it.toString()));
                break;
            case USER:
            case USERLIST:
                Set<UserFieldValue> userFieldValues = new HashSet<>();
                if (value instanceof UserFieldValue) {
                    userFieldValues.add((UserFieldValue) value);
                } else {
                    userFieldValues = ((UserListFieldValue) value).getUserValues();
                }
                userFieldValues.forEach(userFieldValue -> values.getValue().add(userFieldValue.getId()));
                break;
            case FILE:
            case FILELIST:
                Set<FileFieldValue> fileFieldValues = new HashSet<>();
                if (value instanceof FileFieldValue) {
                    fileFieldValues.add((FileFieldValue) value);
                } else {
                    fileFieldValues = ((FileListFieldValue) value).getNamesPaths();
                }
                fileFieldValues.forEach(fieldValue -> values.getValue().add(fieldValue.getName()));
                break;
            case I18N:
                exportI18NString((I18nString) value);
                values.getValue().add(value.toString());
                values.setId(((I18nString) value).getKey());
                break;
            default:
                values.getValue().add(value.toString());
                break;
        }
        return values;
    }

    private LocalDateTime convertDateToLocalDateTime(Date value) {
        return value.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private BigInteger exportInteger(Integer priority) {
        return priority == null ? null : BigInteger.valueOf(priority);
    }

    private TaskTrigger exportTriggers(List<Trigger> triggers) {
        if (triggers == null || triggers.isEmpty()) {
            return null;
        }
        TaskTrigger taskTrigger = objectFactory.createTaskTrigger();
        triggers.forEach(trigger -> {
            TriggerWithId xmlTrigger = objectFactory.createTriggerWithId();
            xmlTrigger.setId(trigger.get_id().toString());
            TriggerType triggerTypeString;
            if (trigger instanceof AutoTrigger) {
                triggerTypeString = TriggerType.AUTO;
            } else if (trigger instanceof TimeTrigger) {
                triggerTypeString = TriggerType.TIME;
            } else {
                triggerTypeString = TriggerType.USER;
            }
            xmlTrigger.setType(triggerTypeString);
            taskTrigger.getTrigger().add(xmlTrigger);
        });
        return taskTrigger;
    }

    private AssignedUserPolicies exportAssignedUserPolicy(Map<String, Boolean> assignedUserPolicy) {
        AssignedUserPolicies assignedUserPolicies = objectFactory.createAssignedUserPolicies();
        assignedUserPolicies.getAssignedUserPolicy().addAll(exportBooleanMap(assignedUserPolicy));
        return assignedUserPolicies;
    }

    private Tags exportTags(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        Tags xmlTags = objectFactory.createTags();
        tags.forEach((key, value) -> {
            Tag tag = new Tag();
            tag.setKey(key);
            tag.setValue(value);
            xmlTags.getTag().add(tag);
        });
        return xmlTags;
    }

    private I18NStringType exportI18NString(I18nString i18nString) {
        if (i18nString == null) {
            return null;
        }
        I18NStringType i18NStringType = objectFactory.createI18NStringType();
//        todo i18n name is not kept after petri net import, i18n import needs to be refactored as a whole
        i18NStringType.setName(i18nString.getKey());
        i18NStringType.setValue(i18nString.getDefaultValue());
        exportTranslations(i18nString.getTranslations(), i18nString.getKey());
        return i18NStringType;
    }

    private String exportLocalDateTime(LocalDateTime toExport) {
//        todo LocalDate and DateTime do not store information about Zone/Offset, Date and DateTime fields needs to be refactored to store values as either Zoned or Offset dateTime
        return toExport == null ? null : toExport.atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private StringCollection exportCollectionOfStrings(Collection<String> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        StringCollection stringCollection = objectFactory.createStringCollection();
        stringCollection.getValue().addAll(collection);
        return stringCollection;
    }

    private MapXsdType exportMapXsdType(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        MapXsdType xsdType = objectFactory.createMapXsdType();
        map.forEach((key, value) -> {
            IntegerMapEntry mapEntry = objectFactory.createIntegerMapEntry();
            mapEntry.setKey(key);
            mapEntry.setValue(exportInteger(value));
            xsdType.getEntry().add(mapEntry);
        });
        return xsdType;
    }

    private PermissionMap exportPermissions(Map<String, Map<String, Boolean>> permissions) {
        PermissionMap permissionMap = objectFactory.createPermissionMap();
        permissions.forEach((key, value) -> {
            Permissions permission = objectFactory.createPermissions();
            permission.setId(key);
            permission.getPermission().addAll(exportBooleanMap(value));
            permissionMap.getEntry().add(permission);
        });
        return permissionMap;
    }

    private List<BooleanMapEntry> exportBooleanMap(Map<String, Boolean> toExport) {
        List<BooleanMapEntry> booleanMapEntryList = new ArrayList<>();
        toExport.forEach((id, permissionValue) -> {
            BooleanMapEntry mapEntry = objectFactory.createBooleanMapEntry();
            mapEntry.setKey(id);
            mapEntry.setValue(permissionValue);
            booleanMapEntryList.add(mapEntry);
        });
        return booleanMapEntryList;
    }
}
