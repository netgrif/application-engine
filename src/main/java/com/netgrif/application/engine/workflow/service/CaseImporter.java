package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.files.StorageResolverService;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.importer.model.*;
import com.netgrif.application.engine.importer.service.ComponentFactory;
import com.netgrif.application.engine.petrinet.domain.*;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.Transaction;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.petrinet.service.PetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.utils.ImporterUtils;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.triggers.TimeTrigger;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
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
    private ComponentFactory componentFactory;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IProcessRoleService processRoleService;

    @Autowired
    private StorageResolverService storageResolverService;

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
            if (this.importedCase == null) {
                continue;
            }
            importedCases.add(workflowService.save(this.importedCase));
            this.importedCase = null;
        }
        return importedCases;
    }

    @Transactional
    protected void importCase(com.netgrif.application.engine.importer.model.Case xmlCase) {
        this.xmlCase = xmlCase;
        Version version = new Version();
        PetriNet model = petriNetService.getPetriNet(xmlCase.getProcessIdentifier(), version);
        if (model == null) {
//            todo throw error?
            log.error("Petri net with identifier [{}] not found, skipping case import", xmlCase.getProcessIdentifier());
            return;
        }
        ProcessResourceId importedCaseId = new ProcessResourceId(model.getStringId(), xmlCase.getId().split("-")[1].trim());
        try {
            workflowService.findOne(importedCaseId.toString());
            log.warn("Case with id [{}] already exists, new id will be generated for imported case", importedCaseId);
            this.importedCase = new Case(model);
        } catch (IllegalArgumentException e) {
            this.importedCase = new Case(model, importedCaseId);
        }
        importCaseMetadata();
        importDataSet();
        importTasks();
    }

    @Transactional
    protected void importTasks() {
        List<Task> importedTasks = new ArrayList<>();
        if (xmlCase.getTask() == null) {
            return;
        }
        this.xmlCase.getTask().forEach(task -> {
            Transition transition = this.importedCase.getPetriNet().getTransition(task.getTransitionId());
            final Task importedTask = Task.with()
                    .title(transition.getTitle())
                    ._id(new ProcessResourceId(this.importedCase.getPetriNetId(), task.getId().split("-")[1].trim()))
                    .caseId(this.importedCase.getStringId())
                    .processId(this.importedCase.getProcessIdentifier())
                    .transitionId(transition.getImportId())
                    .layout(transition.getLayout())
                    .tags(transition.getTags())
                    .userId(task.getUserId())
                    .startDate(parseDateTimeFromXml(task.getStartDate()))
                    .finishDate(parseDateTimeFromXml(task.getFinishDate()))
                    .finishedBy(task.getFinishedBy())
                    .caseColor(this.importedCase.getColor())
                    .caseTitle(this.importedCase.getTitle())
                    .priority(transition.getPriority())
                    .icon(transition.getIcon() == null ? this.importedCase.getIcon() : transition.getIcon())
                    .immediateDataFields(new LinkedHashSet<>(transition.getImmediateData()))
                    .assignPolicy(transition.getAssignPolicy())
                    .dataFocusPolicy(transition.getDataFocusPolicy())
                    .finishPolicy(transition.getFinishPolicy())
                    .build();
            transition.getEvents().forEach((type, event) -> importedTask.addEventTitle(type, event.getTitle()));
            importedTask.addAssignedUserPolicy(transition.getAssignedUserPolicy());
            for (Trigger trigger : transition.getTriggers()) {
                Trigger taskTrigger = trigger.clone();
                importedTask.addTrigger(taskTrigger);

                if (taskTrigger instanceof TimeTrigger timeTrigger) {
                    taskService.scheduleTaskExecution(importedTask, timeTrigger.getStartDate(), this.importedCase);
                }
            }
            ProcessRole defaultRole = processRoleService.defaultRole();
            ProcessRole anonymousRole = processRoleService.anonymousRole();
            for (Map.Entry<String, Map<String, Boolean>> entry : transition.getRoles().entrySet()) {
                if (this.importedCase.getEnabledRoles().contains(entry.getKey())
                        || defaultRole.getStringId().equals(entry.getKey())
                        || anonymousRole.getStringId().equals(entry.getKey())) {
                    importedTask.addRole(entry.getKey(), entry.getValue());
                }
            }
            transition.getNegativeViewRoles().forEach(importedTask::addNegativeViewRole);

            for (Map.Entry<String, Map<String, Boolean>> entry : transition.getUserRefs().entrySet()) {
                importedTask.addUserRef(entry.getKey(), entry.getValue());
            }
            importedTask.resolveViewRoles();
            importedTask.resolveViewUserRefs();

            Transaction transaction = this.importedCase.getPetriNet().getTransactionByTransition(transition);
            if (transaction != null) {
                importedTask.setTransactionId(transaction.getStringId());
            }

            importedTasks.add(importedTask);
            importedCase.addTask(importedTask);
        });
        taskService.save(importedTasks);
    }

    @Transactional
    protected void importDataSet() {
        xmlCase.getDataField().forEach(field -> {
            DataField dataField = new DataField();
            dataField.setEncryption(field.getEncryption());
            dataField.setLastModified(parseDateTimeFromXml(field.getLastModified()));
            dataField.setVersion(field.getVersion());
            if (field.getType() == DataType.FILTER) {
                dataField.setFilterMetadata(parseFilterMetadata(field.getFilterMetadata()));
            }
            dataField.setValue(parseXmlValue(field));
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
        if (predicateMetadata == null || predicateMetadata.getPredicate() == null) {
            return null;
        }
        List<List<Object>> values = new ArrayList<>();
        predicateMetadata.getPredicate().forEach(predicate -> {
            if (predicate == null) {
                return;
            }
            List<Object> value = new ArrayList<>();
            predicate.getData().forEach(data -> {
                if (data == null) {
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
        if (configuration == null) {
            return null;
        }
        Map<String, String> configurationMap = new HashMap<>();
        configuration.getValue().forEach(data -> {
            if (data == null) {
                return;
            }
            configurationMap.put(data.getId(), data.getValue());
        });
        return configurationMap;
    }

    private Object parseXmlValue(com.netgrif.application.engine.importer.model.DataField field) {
        StringCollection value = field.getValues();
        DataType dataType = field.getType();
        if (value == null || value.getValue() == null || value.getValue().isEmpty()) {
            return null;
        }
        Object parsedValue;
        switch (dataType) {
            case DATE:
            case DATE_TIME:
                parsedValue = parseDateTimeFromXml(value.getValue().getFirst());
                if (dataType == com.netgrif.application.engine.importer.model.DataType.DATE) {
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
                if (dataType == com.netgrif.application.engine.importer.model.DataType.MULTICHOICE) {
                    parsedValue = ((Set<String>) parsedValue).stream().map(it -> {
                        I18nString i18nString = new I18nString(it);
                        i18nString.setTranslations(parseTranslations(i18nString.getKey()));
                        return i18nString;
                    }).collect(Collectors.toCollection(LinkedHashSet::new));
                }
                break;
            case USER:
                parsedValue = parseUserFieldValue(value.getValue().getFirst());
                break;
            case USER_LIST:
                parsedValue = new UserListFieldValue(value.getValue().stream().map(this::parseUserFieldValue).collect(Collectors.toList()));
                break;
            case FILE:
                parsedValue = createFileFieldValue(field, value.getValue().getFirst());
                break;
            case FILE_LIST:
                FileListFieldValue fileListValue = new FileListFieldValue();
                value.getValue().forEach(fileName -> fileListValue.getNamesPaths().add(createFileFieldValue(field, fileName)));
                parsedValue = fileListValue;
                break;
            case ENUMERATION:
            case I_18_N:
                parsedValue = new I18nString(value.getValue().getFirst());
                ((I18nString) parsedValue).setTranslations(parseTranslations(value.getId()));
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

    private Map<String, String> parseTranslations(String name) {
        Map<String, String> translations = new HashMap<>();
        this.xmlCase.getI18N().forEach(i18n -> i18n.getI18NString().stream()
                .filter(i18nString -> i18nString.getName().equals(name))
                .forEach(translation -> translations.put(translation.getName(), translation.getName())));
        return translations;
    }

    private FileFieldValue createFileFieldValue(com.netgrif.application.engine.importer.model.DataField field, String fileName) {
        IStorageService storageService = storageResolverService.resolve(((StorageField<?>) importedCase.getField(field.getId())).getStorageType());
        String path = storageService.getPath(this.importedCase.getStringId(), field.getId(), fileName);
        return new FileFieldValue(fileName, path);
    }

    private UserFieldValue parseUserFieldValue(String xmlValue) {
        IUser user;
        try {
            user = userService.resolveById(xmlValue, true);
            return new UserFieldValue(user);
        } catch (IllegalArgumentException e) {
            log.error("User with id [{}] not found, setting empty value", xmlValue);
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
            taskBehavior.getBehavior().forEach(behavior -> behaviorSet.add(FieldBehavior.fromString(behavior)));
            behaviorMap.put(taskBehavior.getTaskId(), behaviorSet);
        });
        return behaviorMap;
    }

    private Map<String, I18nString> parseXmlOptions(Options options) {
        Map<String, I18nString> optionsMap = new HashMap<>();
        if (options == null || options.getOption() == null) {
            return optionsMap;
        }
        options.getOption().forEach(option -> optionsMap.put(option.getKey(), parseXmlI18nString(option)));
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
        parsedI18n.setTranslations(parseTranslations(xmlI18nString.getName()));
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
        importedCase.setViewUserRefs(parseStringCollection(xmlCase.getViewUserRefs()));
        importedCase.setViewUsers(parseStringCollection(xmlCase.getViewUsers()));
        importedCase.setNegativeViewUsers(parseStringCollection(xmlCase.getNegativeViewUsers()));
        importedCase.setConsumedTokens(parseMapXsdType(xmlCase.getConsumedTokens()));
        importedCase.setUsers(parsePermissionMap(xmlCase.getUsers()));
        importedCase.setTitle(xmlCase.getTitle());
        importedCase.getPetriNet().initializeArcs(importedCase.getDataSet());
        updateCaseState();
    }

    private void updateCaseState() {
        Map<String, Integer> activePlaces = parseMapXsdType(xmlCase.getActivePlaces());
        this.importedCase.getPetriNet().getPlaces().forEach((placeId, place) -> place.setTokens(activePlaces.getOrDefault(placeId, 0)));
        this.importedCase.setActivePlaces(activePlaces);
        workflowService.updateMarking(this.importedCase);
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
        consumedTokens.getEntry().forEach(entry -> map.put(entry.getKey(), entry.getValue().intValue()));
        return map;
    }

    @Transactional
    protected void unmarshallXml(InputStream xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Cases.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        xmlCases = (Cases) jaxbUnmarshaller.unmarshal(xml);
    }

    private LocalDateTime parseDateTimeFromXml(String xmlDateTimeString) {
        return xmlDateTimeString == null ? null : LocalDateTime.parse(xmlDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
