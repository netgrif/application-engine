package com.netgrif.application.engine.workflow.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.DefaultFiltersRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.utils.InputStreamToString;
import com.netgrif.application.engine.workflow.domain.*;
import com.netgrif.application.engine.workflow.domain.menu.Menu;
import com.netgrif.application.engine.workflow.domain.menu.MenuAndFilters;
import com.netgrif.application.engine.workflow.domain.menu.MenuEntry;
import com.netgrif.application.engine.workflow.domain.menu.MenuEntryRole;
import com.netgrif.application.engine.workflow.service.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class MenuImportExportService implements IMenuImportExportService {

    private static final Logger log = LoggerFactory.getLogger(MenuImportExportService.class);

    private static final String MENU_ITEM_NAME = "entry_name";
    private static final String USE_ICON = "use_icon";
    private static final String ALLOWED_ROLES = "allowed_roles";
    private static final String BANNED_ROLES = "banned_roles";
    private static final String MENU_IDENTIFIER = "menu_identifier";
    private static final String PARENT_ID = "parentId";
    private static final String ENTRY_DEFAULT_NAME = "entry_default_name";
    private static final String GROUP_NAV_TASK = "navigationMenuConfig";

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
    private IFilterImportExportService filterImportExportService;

    /**
     * Method which performs export of selected menu entries with their filters into xml file.
     * Method finds all cases by provided ids, transform them into FilterImportExportList object
     * and serialize them into xml file on path: storage/filterExport/<userId>/filters.xml
     *
     * @param menusForExport - EnumerationMapField with Ids (delimited by ",") of menu entries as keys
     *                       and identifier of menu they belong to as value
     * @param groupId        case Id of active group
     * @return FileFieldValue - file field value of active group file field used to store exported file
     * @throws IOException - if file which contains exported menus cannot be created
     */
    @Override
    public FileFieldValue exportMenu(EnumerationMapField menusForExport, String groupId, FileField fileField) throws IOException {
        log.info("Exporting menu");
        MenuAndFilters menuAndFilters = new MenuAndFilters();

        for (Map.Entry<String, I18nString> option : menusForExport.getOptions().entrySet()) {
            Menu menu = new Menu();
            menu.setMenuIdentifier(option.getValue().toString());

            List<String> menuItemCaseIds = Arrays.asList(option.getKey().split(","));
            List<String> filterCaseIds = new ArrayList<>();
            menu.setMenuEntries(new ArrayList<>());

            menuItemCaseIds.forEach(menuItemCaseId -> {
                Case menuItem = workflowService.findOne(menuItemCaseId);
                String filterId = ((List<String>) menuItem.getDataSet().get("filter_case").getValue()).get(0);
                menu.getMenuEntries().add(createMenuEntryExportClass(menuItem, filterId));
                filterCaseIds.add(filterId);
            });
            menuAndFilters.getFilterList().getFilters().addAll(filterImportExportService.exportFilters(filterCaseIds).getFilters());

            menuAndFilters.getMenuList().getMenus().add(menu);
        }
        return createXML(menuAndFilters, groupId, fileField);
    }

    /**
     * Method which performs import of menus from uploaded xml file.
     * Method firstly loads xml file from file field and validates it against xml schema for menusWithFilters
     * located on path: menu_export_schema.xml.
     * Then it calls import of filters provided in uploaded xml file.
     * Method then deletes any Preference filter item cases of active group case with same menu identifier
     * as any of the menus imported.
     * If the file is correct, method creates new instance of Preference filter item cases
     * for each menu entry by calling method "createMenuItemCase".
     *
     * @param menuItemCases - list of Preference filter item cases in active group
     * @param ffv           - file field from active group case containing uploaded xml file.
     * @param parentId      - id of active group case
     * @return List<String> - list of values delimited by "," containing
     * preferenceItem case Id, filter case Id, boolean value determining if Icon should be displayed
     * @throws IOException              - if imported file is not found
     * @throws IllegalMenuFileException - if uploaded xml is not in correct xml format and invalidate against schema
     */
    @Override
    public List<String> importMenu(List<Case> menuItemCases, FileFieldValue ffv, String parentId) throws IOException, IllegalMenuFileException, TransitionNotExecutableException {
        StringBuilder resultMessage = new StringBuilder("");

        List<String> importedEntryAndFilterCaseIds = new ArrayList<>();
        MenuAndFilters menuAndFilters = loadFromXML(ffv);

        //Firstly, remove existing preference_filter_item cases having the same menu identifier as any menu
        // which is being currently imported.
        List<String> menuItemIdsToReplace = menuItemCases.stream().filter(caze -> menuAndFilters.getMenuList().getMenus().stream()
                        .anyMatch(menu -> Objects.equals(menu.getMenuIdentifier(), caze.getDataSet().get(MENU_IDENTIFIER).getValue())))
                .map(Case::getStringId).collect(Collectors.toList());

        //Change remove_option button value to trigger its SET action
        if (!menuItemIdsToReplace.isEmpty()) menuItemIdsToReplace.forEach(id -> {
            Map<String, Map<String, String>> caseToRemoveData = new HashMap<>();
            Map<String, String> removeBtnData = new HashMap<>();
            removeBtnData.put("type", "button");
            removeBtnData.put("value", "removed");
            caseToRemoveData.put("remove_option", removeBtnData);

            Case caseToRemove = workflowService.findOne(id);
            QTask qTask = new QTask("task");
            Task task = taskService.searchOne(qTask.transitionId.eq("view").and(qTask.caseId.eq(caseToRemove.getStringId())));
            dataService.setData(task, ImportHelper.populateDataset(caseToRemoveData));
        });

        //Import filters
        Map<String, String> importedFilterTaskIds = filterImportExportService.importFilters(menuAndFilters.getFilterList());

        //Import each menu individually
        menuAndFilters.getMenuList().getMenus().forEach(menu -> {
            resultMessage.append("\nIMPORTING MENU \"").append(menu.getMenuIdentifier()).append("\":\n");
            menu.getMenuEntries().forEach(menuItem -> {
                String entryAndFilterCaseId = createMenuItemCase(resultMessage, menuItem, menu.getMenuIdentifier(), parentId, importedFilterTaskIds.get(menuItem.getFilterCaseId()));
                if (!entryAndFilterCaseId.equals("")) importedEntryAndFilterCaseIds.add(entryAndFilterCaseId);
            });

            QTask qTask = new QTask("task");
            Task task = taskService.searchOne(qTask.transitionId.eq(GROUP_NAV_TASK).and(qTask.caseId.eq(parentId)));

            Map<String, Map<String, String>> groupData = new HashMap<>();
            Map<String, String> groupImportResultMessage = new HashMap<>();
            groupImportResultMessage.put("type", "text");
            groupImportResultMessage.put("value", resultMessage.toString());
            groupData.put("import_results", groupImportResultMessage);
            dataService.setData(task, ImportHelper.populateDataset(groupData));
        });

        importedFilterTaskIds.values().forEach(taskId -> {
            Task importedFilterTask = taskService.findOne(taskId);
            Case filterCase = workflowService.findOne(importedFilterTask.getCaseId());
            try {
                taskService.assignTask(importedFilterTask.getStringId());
                taskService.finishTask(importedFilterTask.getStringId());
                workflowService.save(filterCase);
            } catch (TransitionNotExecutableException e) {
                log.error("Failed to execute \"import_filter\" task with id: " + taskId, e);
            }
        });

        return importedEntryAndFilterCaseIds;
    }

    /**
     * Method which creates new Preference filter item case from imported menuEntry class.
     * Before case creation method prepares allowed and banned roles data for the entry while performing checks
     * for imported filter and role nets in engine.
     *
     * @param item           - imported MenuEntry item
     * @param menuIdentifier - Identifier of new imported menu.
     * @param parentId       - id of active group case
     * @param filterTaskId   - task id of imported filter belonging to men item case being created
     * @return String - values delimited by "," containing case Id of newly created Preference filter item case,
     * Id of its filter case, boolean value determining if Preference filter item case should display icon.
     */
    @Override
    public String createMenuItemCase(StringBuilder resultMessage, MenuEntry item, String menuIdentifier, String parentId, String filterTaskId) {
        AtomicBoolean netCheck = new AtomicBoolean(true);
        resultMessage.append("\nMenu entry \"").append(item.getEntryName()).append("\": ");

        Task importedFilterTask = taskService.findOne(filterTaskId);
        Case filterCase = workflowService.findOne(importedFilterTask.getCaseId());
        if (filterCase == null) {
            resultMessage.append("Filter not found! Menu entry was skipped.\n");
            return "";
        }

        //Creating role entries for allowed_roles/banned_roles datafields
        Map<String, I18nString> allowedRoles = new LinkedHashMap<>();
        Map<String, I18nString> bannedRoles = new LinkedHashMap<>();

        //Checks if role nets and imported filters are present in engine and prepares role constrictions data.
        if (item.getMenuEntryRoleList() != null && !item.getMenuEntryRoleList().isEmpty()) {
            item.getMenuEntryRoleList().forEach(menuEntryRole -> {
                String roleImportId = menuEntryRole.getRoleImportId();
                String netImportId = menuEntryRole.getNetImportId();
                if (netImportId != null) {
                    PetriNet net = petriNetService.getNewestVersionByIdentifier(netImportId);
                    if (net == null) {
                        resultMessage.append("\n- Missing net with import ID: \"").append(netImportId).append("\"").append("for role ").append(roleImportId).append("\n");
                        netCheck.set(false);
                    } else {
                        Optional<ProcessRole> role = net.getRoles().values().stream().filter(r -> r.getImportId().equals(roleImportId))
                                .findFirst();
                        if (role.isPresent()) {
                            //TODO: extend funcionality - add translations to nets and roles
                            if (menuEntryRole.getAuthorizationType().equals(AuthorizationType.allowed)) {
                                allowedRoles.put(roleImportId + ":" + netImportId, new I18nString(role.get().getName() + "(" + net.getTitle() + ")"));
                            } else {
                                bannedRoles.put(roleImportId + ":" + netImportId, new I18nString(role.get().getName() + "(" + net.getTitle() + ")"));
                            }
                        } else {
                            resultMessage.append("\n- Role with import ID \"").append(roleImportId).append("\" ").append("does not exist in the latest present version of net \"").append(netImportId).append("\"\n");
                            netCheck.set(false);
                        }
                    }
                }
            });
        }
        //Creating new Case of preference_filter_item net and setting its data...
        Case menuItemCase = workflowService.createCase(
                petriNetService.getNewestVersionByIdentifier("preference_filter_item").getStringId(),
                item.getEntryName() + "_" + menuIdentifier,
                "",
                userService.getSystem().transformToLoggedUser()
        ).getCase();

        QTask qTask = new QTask("task");
        Task task = taskService.searchOne(qTask.transitionId.eq("init").and(qTask.caseId.eq(menuItemCase.getStringId())));
        try {
            taskService.assignTask(task, userService.getLoggedUser());
            menuItemCase.getDataSet().get(MENU_IDENTIFIER).setValue(menuIdentifier);
            menuItemCase.getDataSet().get(PARENT_ID).setValue(parentId);
            menuItemCase.getDataSet().get(ALLOWED_ROLES).setOptions(allowedRoles);
            menuItemCase.getDataSet().get(BANNED_ROLES).setOptions(bannedRoles);
            workflowService.save(menuItemCase);
        } catch (TransitionNotExecutableException e) {
            log.error("Failed to execute \"init\" task on preference filter item case with id: " + menuItemCase.getStringId(), e);
            netCheck.set(false);
            resultMessage.append("- Failed to execute \"init\" task");
        }
        if (netCheck.get()) resultMessage.append("OK\n");

        return task.getCaseId() + "," + filterCase.getStringId() + "," + item.getUseIcon().toString();
    }

    @Transactional
    protected MenuAndFilters loadFromXML(FileFieldValue ffv) throws IOException, IllegalMenuFileException {
        File f = new File(ffv.getPath());
        validateFilterXML(new FileInputStream(f));
        SimpleModule module = new SimpleModule().addDeserializer(Object.class, FilterDeserializer.getInstance());
        XmlMapper xmlMapper = (XmlMapper) new XmlMapper().registerModule(module);
        String xml = InputStreamToString.inputStreamToString(new FileInputStream(f));
        return xmlMapper.readValue(xml, MenuAndFilters.class);
    }

    @Transactional
    protected FileFieldValue createXML(MenuAndFilters menuAndFilters, String parentId, FileField fileField) throws IOException {
        FileFieldValue ffv = new FileFieldValue();
        try {
            ffv.setName("menu_" + userService.getLoggedUser().getFullName().replaceAll("\\s+", "") + ".xml");
            ffv.setPath(ffv.getPath(parentId, fileField.getImportId()));
            File f = new File(ffv.getPath());
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            xmlMapper.writeValue(baos, menuAndFilters);

            FileOutputStream fos = new FileOutputStream(f);
            baos.writeTo(fos);
        } catch (Exception e) {
            log.error("Failed to export menu!", e);
        }
        return ffv;
    }

    private MenuEntry createMenuEntryExportClass(Case menuItemCase, String filterCaseId) {
        Map<String, I18nString> allowedRoles = menuItemCase.getDataSet().get(ALLOWED_ROLES).getOptions();
        Map<String, I18nString> bannedRoles = menuItemCase.getDataSet().get(BANNED_ROLES).getOptions();

        List<MenuEntryRole> menuEntryRoleList = new ArrayList<>();

        if (allowedRoles != null && !allowedRoles.isEmpty()) {
            menuEntryRoleList.addAll(allowedRoles.keySet().stream().map(roleNet -> {
                MenuEntryRole newMenuEntryRole = new MenuEntryRole();
                newMenuEntryRole.setRoleImportId(roleNet.split(":")[0]);
                newMenuEntryRole.setNetImportId(roleNet.split(":")[1]);
                newMenuEntryRole.setAuthorizationType(AuthorizationType.allowed);
                return newMenuEntryRole;
            }).collect(Collectors.toList()));
        }

        if (bannedRoles != null && !bannedRoles.isEmpty()) {
            menuEntryRoleList.addAll(bannedRoles.keySet().stream().map(roleNet -> {
                MenuEntryRole newMenuEntryRole = new MenuEntryRole();
                newMenuEntryRole.setRoleImportId(roleNet.split(":")[0]);
                newMenuEntryRole.setNetImportId(roleNet.split(":")[1]);
                newMenuEntryRole.setAuthorizationType(AuthorizationType.banned);
                return newMenuEntryRole;
            }).collect(Collectors.toList()));
        }

        MenuEntry exportMenuItem = new MenuEntry();
        exportMenuItem.setEntryName(menuItemCase.getDataSet().get(MENU_ITEM_NAME).toString());
        exportMenuItem.setFilterCaseId(filterCaseId);
        exportMenuItem.setUseIcon((Boolean) menuItemCase.getDataSet().get(USE_ICON).getValue());
        if (!menuEntryRoleList.isEmpty()) exportMenuItem.setMenuEntryRoleList(menuEntryRoleList);

        return exportMenuItem;
    }

    @Override
    public Map<String, I18nString> createAvailableEntriesChoices(List<Case> menuItemCases) {
        Map<String, I18nString> availableItems;
        availableItems = menuItemCases.stream()
                .collect(Collectors.toMap(Case::getStringId, v -> new I18nString((String) v.getDataSet().get(ENTRY_DEFAULT_NAME).getValue())));

        return availableItems;
    }

    @Override
    public Map<String, I18nString> addSelectedEntriesToExport(MultichoiceMapField availableEntries, EnumerationMapField menusForExport, String menuIdentifier) {
        Map<String, I18nString> updatedOptions = new LinkedHashMap<>(menusForExport.getOptions());
        String menuCaseIds = "";
        if (availableEntries.getOptions().size() != 0) {
            for (String id : availableEntries.getValue()) {
                menuCaseIds = menuCaseIds.concat(id + ",");
            }
            updatedOptions.put(menuCaseIds, new I18nString(menuIdentifier));
        }
        return updatedOptions;
    }

    private static void validateFilterXML(InputStream xml) throws IllegalMenuFileException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(FilterImportExportService.class.getResource("/petriNets/menu_export_schema.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
        } catch (Exception ex) {
            throw new IllegalMenuFileException(ex);
        }
    }
}