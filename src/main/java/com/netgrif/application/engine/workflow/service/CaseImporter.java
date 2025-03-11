package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.importer.model.*;
import com.netgrif.application.engine.importer.service.ComponentFactory;
import com.netgrif.application.engine.importer.service.TriggerFactory;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy;
import com.netgrif.application.engine.petrinet.domain.policies.DataFocusPolicy;
import com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.petrinet.service.PetriNetService;
import com.netgrif.application.engine.utils.ImporterUtils;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CaseImporter {

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private PetriNetService petriNetService;

    @Autowired
    private IUserService userService;

    @Autowired
    protected ComponentFactory componentFactory;

    @Autowired
    protected TriggerFactory triggerFactory;

    @Autowired
    protected ITaskService taskService;

    private Cases xmlCases;
    private Case importedCase;
    private com.netgrif.application.engine.importer.model.Case xmlCase;


    @Transactional
    public List<Case> importCases(InputStream xml) {
        List<Case> importedCases = new ArrayList<>();
        try {
            unmarshallXml(xml);
        } catch (JAXBException e) {
            log.error("Error unmarshalling input xml file: ", e);
            return Collections.emptyList();
        }
        for (com.netgrif.application.engine.importer.model.Case xmlCase : xmlCases.getCase()) {
            importCase(xmlCase);
            if (importedCase == null) {
                continue;
            }
            importedCases.add(importedCase);
        }
        return importedCases;
    }

    @Transactional
    protected Case importCase(com.netgrif.application.engine.importer.model.Case xmlCase) {
        this.xmlCase = xmlCase;
        Version version = new Version();
        PetriNet model = petriNetService.getPetriNet(xmlCase.getProcessIdentifier(), version);
        if (model == null) {
//            todo throw error
            log.error("Petri net with identifier [" + xmlCase.getProcessIdentifier() + "] not found, skipping case import");
            return null;
        }
        String importedCaseStringId = xmlCase.getId().split("-")[1].trim();
        try {
            workflowService.findOne(importedCaseStringId);
            this.importedCase = new Case(model, new ProcessResourceId(model.getStringId(), importedCaseStringId.split("-")[1].trim()));
        } catch (IllegalArgumentException e) {
            log.warn("Case with id [{}] already exists, new id will be generated for imported case", xmlCase.getId());
            this.importedCase = new Case(model);
        }
        importCaseMetadata();
        importDataSet();
        importTasks();
        return importedCase;
    }

    @Transactional
    protected void importTasks() {
        List<Task> importedTasks = new ArrayList<>();
        if (xmlCase.getTask() == null) {
            return;
        }
        this.xmlCase.getTask().forEach(task -> {
            Task importedTask = Task.with()
                    .caseId(this.importedCase.getStringId())
                    .transitionId(task.getTransitionId())
                    .title(parseXmlI18nString(task.getTitle()))
                    .priority(task.getPriority() != null ? task.getPriority().intValue() : null)
                    .userId(task.getUserId())
                    .startDate(parseDateTimeFromXml(task.getStartDate()))
                    .finishDate(parseDateTimeFromXml(task.getFinishDate()))
                    .finishedBy(task.getFinishedBy())
                    .transactionId(task.getTransactionId())
                    .icon(task.getIcon())
                    .assignPolicy(AssignPolicy.valueOf(task.getAssignPolicy().value().toUpperCase()))
                    .dataFocusPolicy(DataFocusPolicy.valueOf(task.getDataFocusPolicy().value().toUpperCase()))
                    .finishPolicy(FinishPolicy.valueOf(task.getFinishPolicy().value().toUpperCase()))
                    .tags(task.getTags() != null ? ImporterUtils.buildTagsMap(task.getTags().getTag()) : new HashMap<>())
                    .viewRoles(parseStringCollection(task.getViewRoles()))
                    .viewUserRefs(parseStringCollection(task.getViewUserRefs()))
                    .viewUsers(parseStringCollection(task.getViewUsers()))
                    .negativeViewRoles(parseStringCollection(task.getNegativeViewRoles()))
                    .negativeViewUsers(parseStringCollection(task.getNegativeViewUsers()))
                    .immediateDataFields(new LinkedHashSet<>(parseStringCollection(task.getImmediateDataFields())))
                    .roles(parsePermissionMap(task.getRole())).userRefs(parsePermissionMap(task.getUserRef()))
                    .users(parsePermissionMap(task.getUser()))
                    .assignedUserPolicy(task.getAssignedUserPolicies().getAssignedUserPolicy().stream().collect(Collectors.toMap(BooleanMapEntry::getKey, BooleanMapEntry::isValue)))
                    .triggers(parseXmlTriggers(task.getTriggers()))
                    .build();
            importedTasks.add(importedTask);
            importedCase.addTask(importedTask);
        });
        taskService.save(importedTasks);
    }

    private List<Trigger> parseXmlTriggers(TaskTrigger triggers) {
        List<Trigger> triggerList = new ArrayList<>();
        if (triggers == null) {
            return triggerList;
        }
        triggers.getTrigger().forEach(trigger -> {
            Trigger taskTrigger = triggerFactory.buildTrigger(trigger);
            taskTrigger.set_id(new ObjectId(trigger.getId()));
            triggerList.add(taskTrigger);
        });
        return triggerList;
    }

    @Transactional
    protected void importDataSet() {
        xmlCase.getDataField().forEach(field -> {
            DataField dataField = new DataField();
            dataField.setEncryption(field.getEncryption());
            dataField.setLastModified(parseDateTimeFromXml(field.getLastModified()));
            dataField.setVersion(field.getVersion());
            if(field.getType() == DataType.FILTER) {
                dataField.setFilterMetadata(parseFilterMetadata(field.getFilterMetadata()));
            }
            dataField.setValue(parseXmlValue(field.getValues(), field.getType()));
            dataField.setComponent(parseXmlComponent(field.getComponent()));
            field.getDataRefComponent().stream()
                    .filter(dataRefComponent -> dataRefComponent.getComponent() != null)
                    .forEach(dataRefComponent -> dataField.getDataRefComponents().put(dataRefComponent.getTaskId(), parseXmlComponent(dataRefComponent.getComponent())));
            dataField.setValidations(parseXmlValidations(field.getValidations()));
            dataField.setOptions(parseXmlOptions(field.getOptions()));
            dataField.setBehavior(parseXmlBehaviors(field.getBehaviors()));
            importedCase.getDataSet().put(field.getId(), dataField);
        });
    }

    private Map<String, Object> parseFilterMetadata(FilterMetadata filterMetadata) {
        Map<String, Object> filterMetadataMap = new HashMap<>();
        if (filterMetadata == null) {
            return filterMetadataMap;
        }
        filterMetadataMap.put("filterType", filterMetadata.getFilterType().toString());
        filterMetadataMap.put("predicateMetadata", parsePredicateMetadata(filterMetadata.getPredicateMetadata()));
        filterMetadataMap.put("searchCategories", filterMetadata.getSearchCategories());
        filterMetadataMap.put("defaultSearchCategories", filterMetadata.isDefaultSearchCategories());
        filterMetadataMap.put("inheritAllowedNets", filterMetadata.isInheritAllowedNets());
        return filterMetadataMap;
    }

    private Object parsePredicateMetadata(PredicateTreeMetadata predicateMetadata) {
        if(predicateMetadata == null || predicateMetadata.getPredicate() == null) {
            return null;
        }
        List<List<Object>> values = new ArrayList<>();
        predicateMetadata.getPredicate().forEach(predicate -> {
            if(predicate == null) {
                return;
            }
            List<Object> value = new ArrayList<>();
            predicate.getData().forEach(data -> {
                if(data == null) {
                    return;
                }
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("category", data.getCategory());
                dataMap.put("values", parseStringCollection(data.getValues()));
                dataMap.put("configuration", parseConfiguration(data.getConfiguration()));
                value.add(dataMap);
            });
            values.add(value);
        });
        return values;
    }

    private Object parseConfiguration(CategoryMetadataConfiguration configuration) {
        if(configuration == null) {
            return null;
        }
        Map<String, String> configurationMap = new HashMap<>();
        configuration.getValue().forEach(data -> {
            if(data == null) {
                return;
            }
            configurationMap.put(data.getId(), data.getValue());
        });
        return configurationMap;
    }

    private Object parseXmlValue(StringCollection value, DataType dataType) {
        if (value == null || value.getValue() == null || value.getValue().isEmpty()) {
            return null;
        }
        Object parsedValue;
        switch (dataType) {
            case DATE:
            case DATE_TIME:
                parsedValue = parseDateTimeFromXml(value.getValue().getFirst());
                if (dataType == DataType.DATE) {
                    parsedValue = ((LocalDateTime) parsedValue).toLocalDate();
                }
                break;
            case STRING_COLLECTION:
            case CASE_REF:
            case TASK_REF:
                parsedValue = parseStringCollection(value);
                break;
            case NUMBER:
                parsedValue = Double.parseDouble(value.getValue().getFirst());
                break;
            case BOOLEAN:
                parsedValue = Boolean.parseBoolean(value.getValue().getFirst());
                break;
            case MULTICHOICE:
            case MULTICHOICE_MAP:
                parsedValue = parseStringCollection(value, new LinkedHashSet<>());
                if (dataType == DataType.MULTICHOICE) {
                    parsedValue = ((Set<String>) parsedValue).stream().map(I18nString::new).collect(Collectors.toCollection(LinkedHashSet::new));
                }
                break;
            case USER:
                parsedValue = parseUserFieldValue(value.getValue().getFirst());
                break;
            case USER_LIST:
                parsedValue = new UserListFieldValue(value.getValue().stream().map(this::parseUserFieldValue).collect(Collectors.toList()));
                break;
            case FILE:
//                todo path check/replace if new id for case was generated
                parsedValue = FileFieldValue.fromString(value.getValue().getFirst());
                break;
            case FILE_LIST:
                parsedValue = FileListFieldValue.fromString(value.getValue().getFirst());
                break;
            case ENUMERATION:
            case I_18_N:
                parsedValue = new I18nString(value.getValue().getFirst());
                break;
            case BUTTON:
                parsedValue = Integer.parseInt(value.getValue().getFirst());
                break;
            default:
                parsedValue = value.getValue().getFirst();
                break;
        }
        return parsedValue;
    }

    private UserFieldValue parseUserFieldValue(String xmlValue) {
        IUser user;
        try {
            user = userService.resolveById(xmlValue, true);
            return new UserFieldValue(user);
        } catch (IllegalArgumentException e) {
            log.error("User with id [" + xmlValue + "] not found, setting empty value");
            return new UserFieldValue();
        }
    }

    private Map<String, Set<FieldBehavior>> parseXmlBehaviors(TaskBehaviors behaviors) {
        Map<String, Set<FieldBehavior>> behaviorMap = new HashMap<>();
        if (behaviors == null || behaviors.getTaskBehavior() == null || behaviors.getTaskBehavior().isEmpty()) {
            return behaviorMap;
        }
        behaviors.getTaskBehavior().forEach(taskBehavior -> {
            Set<FieldBehavior> behaviorSet = new HashSet<>();
            taskBehavior.getBehavior().forEach(behavior -> {
                behaviorSet.add(FieldBehavior.fromString(behavior));
            });
            behaviorMap.put(taskBehavior.getTaskId(), behaviorSet);
        });
        return behaviorMap;
    }

    private Map<String, I18nString> parseXmlOptions(Options options) {
        Map<String, I18nString> optionsMap = new HashMap<>();
        if (options == null || options.getOption() == null) {
            return optionsMap;
        }
        options.getOption().forEach(option -> {
            optionsMap.put(option.getKey(), parseXmlI18nString(option));
        });
        return optionsMap;
    }

    private List<Validation> parseXmlValidations(Validations validations) {
        List<Validation> parsedValidations = new ArrayList<>();
        if (validations == null) {
            return parsedValidations;
        }
        validations.getValidation().forEach(validation -> {
//            todo problem with i18n translations, petriNet does not store information about translations on their own, they get resolved during net import and are stored in i18n objects
//            todo export i18ns with their translations? refactor petriNet?
            parsedValidations.add(ImporterUtils.makeValidation(validation.getExpression().getValue(), parseXmlI18nString(validation.getMessage()), validation.getExpression().isDynamic()));
        });
        return parsedValidations;
    }

    private I18nString parseXmlI18nString(I18NStringType xmlI18nString) {
        if (xmlI18nString == null) {
            return null;
        }
        I18nString parsedI18n = new I18nString();
        parsedI18n.setKey(xmlI18nString.getName());
        parsedI18n.setDefaultValue(xmlI18nString.getValue());
        return parsedI18n;
    }

    private Component parseXmlComponent(com.netgrif.application.engine.importer.model.Component component) {
        return component == null ? null : componentFactory.buildComponent(component);
    }

    @Transactional
    protected void importCaseMetadata() {
//        todo id and visualId cannot be set, both are generated in constructor
        IUser user;
        try {
            user = userService.findById(xmlCase.getAuthor(), true);
        } catch (IllegalArgumentException e) {
            log.warn("Author of case to be imported not found, setting technical user as author");
            user = userService.getSystem();
        }
        importedCase.setAuthor(user.transformToAuthor());
        if (xmlCase.getTags() != null) {
            importedCase.setTags(ImporterUtils.buildTagsMap(xmlCase.getTags().getTag()));
        }
//        todo date validate
        importedCase.setUriNodeId(xmlCase.getUriNodeId());
        importedCase.setCreationDate(parseDateTimeFromXml(xmlCase.getCreationDate()));
        importedCase.setLastModified(parseDateTimeFromXml(xmlCase.getLastModified()));
        importedCase.setEnabledRoles(parseStringCollection(xmlCase.getEnabledRoles(), new HashSet<>()));
        importedCase.setViewRoles(parseStringCollection(xmlCase.getViewRoles()));
        importedCase.setViewUserRefs(parseStringCollection(xmlCase.getViewUserRefs()));
        importedCase.setViewUsers(parseStringCollection(xmlCase.getViewUsers()));
        importedCase.setNegativeViewRoles(parseStringCollection(xmlCase.getNegativeViewRoles()));
        importedCase.setNegativeViewUsers(parseStringCollection(xmlCase.getNegativeViewUsers()));
        importedCase.setImmediateDataFields(parseStringCollection(xmlCase.getImmediateDataFields(), new LinkedHashSet<>()));
        importedCase.setConsumedTokens(parseMapXsdType(xmlCase.getConsumedTokens()));
        importedCase.setActivePlaces(parseMapXsdType(xmlCase.getActivePlaces()));
        importedCase.setPermissions(parsePermissionMap(xmlCase.getPermissions()));
        importedCase.setUserRefs(parsePermissionMap(xmlCase.getUserRefs()));
        importedCase.setUsers(parsePermissionMap(xmlCase.getUsers()));
    }

    private List<String> parseStringCollection(StringCollection xmlCollection) {
        return parseStringCollection(xmlCollection, new ArrayList<>());
    }

    private <T extends Collection> T parseStringCollection(StringCollection xmlCollection, T collection) {
        if (xmlCollection != null) {
            collection.addAll(xmlCollection.getValue());
        }
        return collection;
    }

    private Map<String, Map<String, Boolean>> parsePermissionMap(PermissionMap permissions) {
        Map<String, Map<String, Boolean>> permissionMap = new HashMap<>();
        if (permissions == null) {
            return permissionMap;
        }
        permissions.getEntry().forEach(entry -> {
            Map<String, Boolean> permission = new HashMap<>();
            entry.getPermission().forEach(xmlPermission -> permission.put(xmlPermission.getKey(), xmlPermission.isValue()));
            permissionMap.put(entry.getId(), permission);
        });
        return permissionMap;
    }

    private Map<String, Integer> parseMapXsdType(MapXsdType consumedTokens) {
        Map<String, Integer> map = new HashMap<>();
        if (consumedTokens == null) {
            return map;
        }
        consumedTokens.getEntry().forEach(entry -> {
            map.put(entry.getKey(), entry.getValue().intValue());
        });
        return map;
    }

    @Transactional
    protected void unmarshallXml(InputStream xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Cases.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        xmlCases = (Cases) jaxbUnmarshaller.unmarshal(xml);
    }

    private Version parseProcessVersionFromXml(String xmlVersion) {
        String[] versionParts = xmlVersion.split("\\.");
//        todo version validation
        return new Version(Integer.parseInt(versionParts[0].trim()), Integer.parseInt(versionParts[1].trim()), Integer.parseInt(versionParts[2].trim()));
    }

    private LocalDateTime parseDateTimeFromXml(String xmlDateTimeString) {
        return xmlDateTimeString == null ? null : LocalDateTime.parse(xmlDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
