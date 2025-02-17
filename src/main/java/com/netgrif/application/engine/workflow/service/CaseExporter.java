package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.importer.model.*;
import com.netgrif.application.engine.importer.model.Properties;
import com.netgrif.application.engine.petrinet.domain.I18nString;
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

import java.io.OutputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class CaseExporter {

    @Autowired
    private ITaskService taskService;

    private final ObjectFactory objectFactory = new ObjectFactory();

    public void exportCases(Collection<com.netgrif.application.engine.workflow.domain.Case> casesToExport, OutputStream outputStream) {
        casesToExport.forEach(caseToExport -> exportCase(caseToExport, outputStream));
    }

    public void exportCase(com.netgrif.application.engine.workflow.domain.Case caseToExport, OutputStream outputStream) {
        Case xmlCase = objectFactory.createCase();
        exportCaseMetadata(caseToExport, xmlCase);
        List<com.netgrif.application.engine.workflow.domain.Task> tasks = caseToExport.getTasks().stream()
                .map(taskPair -> taskService.findOne(taskPair.getTask()))
                .toList();
        exportTasks(tasks, xmlCase.getTask());
        exportDataFields(caseToExport.getDataSet(), xmlCase.getDataField());
        try {
            marshallCase(xmlCase, outputStream);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private void exportDataFields(LinkedHashMap<String, com.netgrif.application.engine.workflow.domain.DataField> dataSet, List<DataField> dataField) {
        dataSet.forEach((key, value) -> dataField.add(exportDataField(key, value)));
    }

    private DataField exportDataField(String fieldId, com.netgrif.application.engine.workflow.domain.DataField dataFieldToExport) {
        DataField xmlDataField = objectFactory.createDataField();
        xmlDataField.setId(fieldId);
        xmlDataField.setValue(exportDataFieldValue(dataFieldToExport.getValue()));
        xmlDataField.setFilterMetadata(exportDataFieldValue(dataFieldToExport.getFilterMetadata()));
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

    private TaskBehaviors exportTaskBehaviors(Map<String, Set<FieldBehavior>> taskBehavior) {
        if (taskBehavior == null || taskBehavior.isEmpty()) {
            return null;
        }
        TaskBehaviors xmlTaskBehaviors = new TaskBehaviors();
        taskBehavior.forEach((taskId, behaviors) -> {
            Behaviors xmlBehavior = objectFactory.createBehaviors();
            xmlBehavior.setTaskId(taskId);
            behaviors.forEach(behavior -> {
                xmlBehavior.getBehavior().add(Behavior.fromValue(behavior.toString()));
            });
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
        });
        return xmlOptions;
    }

    private Validations exportValidations(List<com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation> validations) {
        if (validations == null || validations.isEmpty()) {
            return null;
        }
        Validations xmlValidations = objectFactory.createValidations();
        validations.forEach(validation -> {
//            todo resolve other Validation object properties after 6.4.0 is merged into 7.0.0
            Validation xmlValidation = objectFactory.createValidation();
            xmlValidation.setMessage(exportI18NString(validation.getValidationMessage()));
            xmlValidations.getValidation().add(xmlValidation);
        });
        return xmlValidations;
    }

    private List<DataRefComponent> exportDataRefComponents(Map<String, com.netgrif.application.engine.petrinet.domain.Component> dataRefComponents) {
        if (dataRefComponents == null || dataRefComponents.isEmpty()) {
            return null;
        }
        List<DataRefComponent> xmlDataRefComponents = new ArrayList<>();
        dataRefComponents.forEach((taskId, component) -> {
            DataRefComponent xmlDataRefComponent = objectFactory.createDataRefComponent();
            Component xmlComponent = exportComponent(component);
            xmlDataRefComponent.setTaskId(taskId);
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

    private String exportDataFieldValue(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private void exportTasks(List<com.netgrif.application.engine.workflow.domain.Task> tasksToExport, List<Task> xmlTasks) {
        tasksToExport.forEach(taskToExport -> xmlTasks.add(exportTask(taskToExport)));
    }

    private Task exportTask(com.netgrif.application.engine.workflow.domain.Task taskToExport) {
        Task xmlTask = objectFactory.createTask();
        xmlTask.setId(taskToExport.getStringId());
        xmlTask.setTransitionId(taskToExport.getTransitionId());
        xmlTask.setTitle(exportI18NString(taskToExport.getTitle()));
        xmlTask.setPriority(BigInteger.valueOf(taskToExport.getPriority()));
        xmlTask.setUserId(taskToExport.getUserId());
        xmlTask.setStartDate(exportLocalDateTime(taskToExport.getStartDate()));
        xmlTask.setFinishDate(exportLocalDateTime(taskToExport.getFinishDate()));
        xmlTask.setFinishedBy(taskToExport.getFinishedBy());
        xmlTask.setTransactionId(taskToExport.getTransactionId());
        xmlTask.setIcon(taskToExport.getIcon());
        xmlTask.setAssignPolicy(AssignPolicy.fromValue(taskToExport.getAssignPolicy().toString()));
        xmlTask.setDataFocusPolicy(DataFocusPolicy.fromValue(taskToExport.getDataFocusPolicy().toString()));
        xmlTask.setFinishPolicy(FinishPolicy.fromValue(taskToExport.getFinishPolicy().toString()));
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

    private TaskTrigger exportTriggers(List<Trigger> triggers) {
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
            taskTrigger.setTrigger(xmlTrigger);
        });
        return taskTrigger;
    }

    private AssignedUserPolicies exportAssignedUserPolicy(Map<String, Boolean> assignedUserPolicy) {
        AssignedUserPolicies assignedUserPolicies = objectFactory.createAssignedUserPolicies();
        assignedUserPolicies.getAssignedUserPolicy().addAll(exportBooleanMap(assignedUserPolicy));
        return assignedUserPolicies;
    }

    protected void marshallCase(com.netgrif.application.engine.importer.model.Case caseToExport, OutputStream outputStream) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(com.netgrif.application.engine.importer.model.Case.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        todo extract to property?
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "https://petriflow.com/petriflow.schema.xsd");
        marshaller.marshal(caseToExport, outputStream);
    }

    private void exportCaseMetadata(com.netgrif.application.engine.workflow.domain.Case caseToExport, Case xmlCase) {
        xmlCase.setId(caseToExport.getStringId());
//        todo should the whole object be exported? is id specific enough? should email be exported instead of id?
        xmlCase.setAuthor(caseToExport.getAuthor().getId());
        xmlCase.setColor(caseToExport.getColor());
        xmlCase.setProcessVersion(caseToExport.getPetriNet().getVersion().toString());
        xmlCase.setProcessIdentifier(xmlCase.getProcessIdentifier());
        xmlCase.setVisualId(xmlCase.getVisualId());
        xmlCase.setUriNodeId(xmlCase.getUriNodeId());
        xmlCase.setTags(exportTags(caseToExport.getTags()));
        xmlCase.setTitle(caseToExport.getTitle());
        xmlCase.setCreationDate(exportLocalDateTime(caseToExport.getCreationDate()));
        xmlCase.setLastModified(exportLocalDateTime(caseToExport.getLastModified()));
        xmlCase.setEnabledRoles(exportCollectionOfStrings(caseToExport.getEnabledRoles()));
        xmlCase.setViewRoles(exportCollectionOfStrings(caseToExport.getViewRoles()));
        xmlCase.setViewUserRefs(exportCollectionOfStrings(caseToExport.getNegativeViewUsers()));
        xmlCase.setViewUsers(exportCollectionOfStrings(caseToExport.getNegativeViewUsers()));
        xmlCase.setNegativeViewRoles(exportCollectionOfStrings(caseToExport.getNegativeViewRoles()));
        xmlCase.setNegativeViewUsers(exportCollectionOfStrings(caseToExport.getNegativeViewUsers()));
        xmlCase.setImmediateDataFields(exportCollectionOfStrings(caseToExport.getImmediateDataFields()));
        xmlCase.setActivePlaces(exportMapXsdType(caseToExport.getActivePlaces()));
        xmlCase.setConsumedTokens(exportMapXsdType(caseToExport.getConsumedTokens()));
        xmlCase.setPermissions(exportPermissions(caseToExport.getPermissions()));
        xmlCase.setUserRefs(exportPermissions(caseToExport.getUserRefs()));
        xmlCase.setUsers(exportPermissions(caseToExport.getUsers()));
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
        I18NStringType i18NStringType = objectFactory.createI18NStringType();
        i18NStringType.setName(i18nString.getKey());
        i18NStringType.setValue(i18nString.getDefaultValue());
        return i18NStringType;
    }

    private String exportLocalDateTime(LocalDateTime toExport) {
        return toExport == null ? null : toExport.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    private StringCollection exportCollectionOfStrings(Collection<String> collection) {
        StringCollection stringCollection = objectFactory.createStringCollection();
        stringCollection.getValue().addAll(collection);
        return stringCollection;
    }

    private MapXsdType exportMapXsdType(Map<String, Integer> map) {
        MapXsdType xsdType = objectFactory.createMapXsdType();
        map.forEach((key, value) -> {
            IntegerMapEntry mapEntry = objectFactory.createIntegerMapEntry();
            mapEntry.setKey(key);
            mapEntry.setValue(BigInteger.valueOf(value));
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
