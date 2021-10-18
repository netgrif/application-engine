package com.netgrif.workflow.workflow.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.netgrif.workflow.AsyncRunner;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.filters.FilterImportExportList;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.FileField;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceMapField;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.startup.DefaultFiltersRunner;
import com.netgrif.workflow.startup.ImportHelper;
import com.netgrif.workflow.workflow.domain.*;
import com.netgrif.workflow.workflow.service.interfaces.*;
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class MenuImportExport implements IMenuImportExport {

    private static final Logger log = LoggerFactory.getLogger(MenuImportExport.class);

    private static final String MENU_ITEM_NAME = "entry_name";
    private static final String USE_ICON = "use_icon";
    private static final String ICON_IDENTIFIER = "icon_name";
    private static final String ALLOWED_ROLES = "allowed_roles";
    private static final String BANNED_ROLES = "banned_roles";
    private static final String ENTRY_TITLE = "entry_default_name";
    private String importResultMessage = "";

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

    @Autowired
    private FileStorageConfiguration fileStorageConfiguration;


    @Override
    public FileFieldValue exportMenu(EnumerationMapField menusForExport, String groupId, FileField fileField) throws IOException {
        log.info("Exporting menu"); //TODO pridat XML class s menuList a FilterList polozkami
        MenuAndFilters menuAndFilters = new MenuAndFilters();

        for (Map.Entry<String, I18nString> option : menusForExport.getOptions().entrySet()) {
            Menu menu = new Menu();
            menu.setMenuIdentifier(option.getValue().toString());

            List<String> menuItemCaseIds = Arrays.asList(option.getKey().split(","));
            List<String> filterCaseIds = new ArrayList<>();
            menu.setMenuEntries(new ArrayList<>());

            menuItemCaseIds.forEach(menuItemCaseId -> {
                Case menuItem = workflowService.findOne(menuItemCaseId);
                String filterId = menuItem.getDataSet().get("filter_case").getValue().toString();
                menu.getMenuEntries().add(createMenuEntryExportClass(menuItem));
                filterCaseIds.add(filterId.substring(1, filterId.length() - 1));
            });
            filterCaseIds.forEach(filterCaseId -> {
                Case filterCase = workflowService.findOne(filterCaseId);
                menuAndFilters.getFilterList().getFilters().add(filterImportExportService.createExportClass(filterCase));
            });

            menuAndFilters.getMenuList().getMenus().add(menu);
        }
        return createXML(menuAndFilters, groupId, fileField);
    }
//            menuItemCases.forEach(menuItem -> {
//                String filterId = menuItem.getDataSet().get("filter_case").getValue().toString();
//                menu.getMenuEntries().add(createMenuItemExportClass(menuItem));
////            menuList.getMenuList().get(0).getMenuEntries().add(createMenuItemExportClass(menuItem));
//                filterCaseIds.add(filterId.substring(1, filterId.length() - 1));
//            });
//            filterCaseIds.forEach(filterId -> {
//                Case filterCase = workflowService.findOne(filterId);
//                menuAndFilters.getFilterList().getFilters().add(filterImportExportService.createExportClass(filterCase, menuIdentifier));
//            });

//            menuAndFilters.getMenuList().getMenuList().add(menu);
//        }



//        List<String> filterCaseIds = new ArrayList<>();
//        menu.setMenuEntries(new ArrayList<>());
////        menuList.getMenuList().get(0).setMenuIdentifier(menuIdentifier);
////        menuList.getMenuList().get(0).setMenuEntries(new ArrayList<>());
//        menuItemCases.forEach(menuItem -> {
//            String filterId = menuItem.getDataSet().get("filter_case").getValue().toString();
//            menu.getMenuEntries().add(createMenuItemExportClass(menuItem));
////            menuList.getMenuList().get(0).getMenuEntries().add(createMenuItemExportClass(menuItem));
//            filterCaseIds.add(filterId.substring(1, filterId.length() - 1));
//        });
//        filterCaseIds.forEach(filterId -> {
//            Case filterCase = workflowService.findOne(filterId);
//            menuAndFilters.getFilterList().getFilters().add(filterImportExportService.createExportClass(filterCase, menuIdentifier));
//        });


    @Override
    public List<String> importMenu(List<Case> menuItemCases, FileFieldValue ffv, String parentId) throws IOException, IllegalMenuFileException {
        importResultMessage = "";
        List<String> filterTaskIds = new ArrayList<>();
        MenuAndFilters menuAndFilters= loadFromXML(ffv);

        //Firstly, remove existing preference_filter_item cases having the same menu identifier as any menu
        // which is being currently imported.
        List<String> menuItemIdsToReplace = menuItemCases.stream().filter(caze -> menuAndFilters.getMenuList().getMenus().stream()
                        .anyMatch(menu -> Objects.equals(menu.getMenuIdentifier(), caze.getDataSet().get("menu_identifier").getValue())))
                .map(Case::getStringId).collect(Collectors.toList());

        //Change remove_option button value to trigger its SET action
        if (!menuItemIdsToReplace.isEmpty()) menuItemIdsToReplace.forEach(id -> {
            Map<String, Map<String, String>> caseToRemoveData = new HashMap<>();
            Map <String, String> removeBtnData = new HashMap<>();
            removeBtnData.put("type", "button");
            removeBtnData.put("value", "removed");
            caseToRemoveData.put("remove_option", removeBtnData);

            Case caseToRemove = workflowService.findOne(id);
            QTask qTask = new QTask("task");
            Task task = taskService.searchOne(qTask.transitionId.eq("view").and(qTask.caseId.eq(caseToRemove.getStringId())));
            dataService.setData(task, ImportHelper.populateDataset(caseToRemoveData));
        });

        //Import filters
        List<String> importedFilterIds = filterImportExportService.importFilters(menuAndFilters.getFilterList());

        //Import each menu individually
        final AtomicInteger cnt = new AtomicInteger( 0 ) ;
        menuAndFilters.getMenuList().getMenus().forEach(menu -> {
            importResultMessage = importResultMessage.concat("\nIMPORTING MENU \"" + menu.getMenuIdentifier() + "\":\n");
            menu.getMenuEntries().forEach(menuItem -> {
                String filterTaskId = createMenuItemCase(menuItem, menu.getMenuIdentifier(), parentId, importedFilterIds.get(cnt.getAndIncrement()));
                if (!filterTaskId.equals("")) filterTaskIds.add(filterTaskId);
            });

//        for (Menu menu : menuAndFilters.getMenuList().getMenuList()) {
//            importResultMessage = importResultMessage.concat("\nIMPORTING MENU \"" + menu.getMenuIdentifier() + "\":\n");
//            menu.getMenuEntries().forEach(menuItem -> {
//                String filterTaskId = createMenuItemCase(menuItem, menu.getMenuIdentifier(), parentId, importedFilterIds.get(cnt.getAndIncrement()));
//                if (!filterTaskId.equals("")) filterTaskIds.add(filterTaskId);
//            });

            QTask qTask = new QTask("task");
            Task task = taskService.searchOne(qTask.transitionId.eq("navigationMenuConfig").and(qTask.caseId.eq(parentId)));

            Map<String, Map<String, String>> groupData = new HashMap<>();
            Map<String, String> groupImportResultMessage = new HashMap<>();
            groupImportResultMessage.put("type", "text");
            groupImportResultMessage.put("value", importResultMessage);
            groupData.put("import_results", groupImportResultMessage);
            dataService.setData(task, ImportHelper.populateDataset(groupData));
//        }
        });

        return filterTaskIds;
    }

    @Transactional
    protected MenuAndFilters loadFromXML(FileFieldValue ffv) throws IOException, IllegalMenuFileException {
        File f = new File(ffv.getPath());
        validateFilterXML(new FileInputStream(f));
        SimpleModule module = new SimpleModule().addDeserializer(Object.class, CustomFilterDeserializer.getInstance());
        XmlMapper xmlMapper = (XmlMapper) new XmlMapper().registerModule(module);
        String xml = inputStreamToString(new FileInputStream(f));
        return xmlMapper.readValue(xml, MenuAndFilters.class);
    }

    @Transactional
    protected FileFieldValue createXML(MenuAndFilters menuAndFilters, String parentId, FileField fileField) throws IOException {
        FileFieldValue ffv = new FileFieldValue();
        try {

            ffv.setName("menu_" + userService.getLoggedUser().getFullName() + ".xml");
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
//            String filePath = ffv.getPath() + userService.getLoggedUser().getName() + ".xml";
//            File f = new File(filePath);
//            f.getParentFile().mkdirs();
//            XmlMapper xmlMapper = new XmlMapper();
//            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
//            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            xmlMapper.writeValue(baos, menuList);
//
//            FileOutputStream fos = new FileOutputStream(f);
//            baos.writeTo(fos);

//            return new FileFieldValue("menu_" + userService.getLoggedUser().getName() + ".xml", filePath);


    @Override
    public String createMenuItemCase(MenuEntry item, String menuIdentifier, String parentId, String filterTaskId) {
        AtomicBoolean netCheck = new AtomicBoolean(true);
        importResultMessage = importResultMessage.concat("\nMenu entry \"" + item.getEntryName() + "\": ");


        Task importedFilterTask = taskService.findOne(filterTaskId);
        Case filterCase = workflowService.findOne(importedFilterTask.getCaseId());
        try {
//            workflowService.save(filterCase);
            taskService.assignTask(importedFilterTask.getStringId());
            taskService.finishTask(importedFilterTask.getStringId());
            workflowService.save(filterCase);

        } catch (TransitionNotExecutableException e) {
            e.printStackTrace();
        }
//        Case filterCase = workflowService.searchOne(QCase.case$.processIdentifier.eq("filter").and(QCase.case$.title.eq(item.getEntry_name())));

        if (filterCase == null) {
            importResultMessage = importResultMessage.concat("Filter not found! Menu entry was skipped.\n");
            return "";
        }

        //Creating role entries for allowed_roles/banned_roles datafields
        Map<String, I18nString> allowedRoles = new LinkedHashMap<>();
        Map<String, I18nString> bannedRoles = new LinkedHashMap<>();

        if (item.getMenuEntryRoleList() != null && !item.getMenuEntryRoleList().isEmpty()) {
            item.getMenuEntryRoleList().forEach(menuEntryRole -> {
                String roleImportId = menuEntryRole.getRoleImportId();
                String netImportId = menuEntryRole.getNetImportId();
                if (netImportId != null) {
                    PetriNet net = petriNetService.getNewestVersionByIdentifier(netImportId);
                    if (net == null) {
                        importResultMessage = importResultMessage.concat("\nMissing net with import ID: \"" + netImportId + "\"" + "for role " + roleImportId + "\n");
                        netCheck.set(false);
                    } else {
                        Optional<ProcessRole> role = net.getRoles().values().stream().filter(r -> r.getImportId().equals(roleImportId))
                                .findFirst();
                        if (role.isPresent()) {
                            if (menuEntryRole.getAuthorizationType().equals(AuthorizationType.ALLOWED)) {
                                allowedRoles.put(roleImportId + ":" + netImportId, new I18nString(role.get().getName() + "(" + net.getTitle() + ")"));
                            } else {
                                bannedRoles.put(roleImportId + ":" + netImportId, new I18nString(role.get().getName() + "(" + net.getTitle() + ")"));
                            }
                        } else {
                            importResultMessage = importResultMessage.concat("\nRole with import ID \"" + roleImportId + "\" " + "is not present in currently uploaded net \"" + netImportId + "\"\n");
                        }
                    }
                }
            });
        }
            //Creating new Case of preference_filter_item net and setting its data...
            Case menuItemCase = workflowService.createCase(petriNetService.getNewestVersionByIdentifier("preference_filter_item").getStringId()
                    , item.getEntryName() + "_" + menuIdentifier, "", userService.getSystem().transformToLoggedUser());

//            Case menuItemCase = workflowService.createCase(petriNetService.getNewestVersionByIdentifier("preference_filter_item").getStringId()
//                    , item.getEntry_name() + "_" + menuIdentifier, "", userService.getLoggedUser().transformToLoggedUser());
//            //TODO title should be empty

            QTask qTask = new QTask("task");
            Task task = taskService.searchOne(qTask.transitionId.eq("init").and(qTask.caseId.eq(menuItemCase.getStringId())));
//            menuItemCase.getDataSet().get("filter_case").setValue(Arrays.asList(filterCase.getStringId()));
//            menuItemCase.getDataSet().get("parentId").setValue(parentId);
//            workflowService.save(menuItemCase);

//            Map<String, Map<String, String>> menuItemData = new HashMap<>();
//            menuItemData.put("filter_case", createFilterMapEntry(filterCase.getStringId(), false));
//            menuItemData.put("parentId", createFilterMapEntry(parentId, true));

            //Setting data on "init" task
            try {
                taskService.assignTask(task, userService.getLoggedUser());

//                menuItemCase.getDataSet().get("filter_case").setValue(Arrays.asList(filterCase.getStringId()));
                menuItemCase.getDataSet().get("menu_identifier").setValue(menuIdentifier);
                menuItemCase.getDataSet().get("parentId").setValue(parentId);
                menuItemCase.getDataSet().get(ALLOWED_ROLES).setOptions(allowedRoles);
                menuItemCase.getDataSet().get(BANNED_ROLES).setOptions(bannedRoles);
                workflowService.save(menuItemCase);

                //TODO use_icon nastavit
                //TODO title a icon sa nastavi podla filterCaseRef na SET akcii
//                menuItemCase.getDataSet().get(ENTRY_TITLE).setValue(item.getEntry_name() + " menu item");
//                menuItemCase.getDataSet().get(ICON_IDENTIFIER).setValue(item.getIconIdentifier());

//                Map<String, Map<String, String>> filterCaseRefData = new HashMap<>();
//                Map <String, String> data = new HashMap<>();
//                data.put("type", "caseRef");
////                data.put("value", "[" + filterCase.getStringId() + "]");
//                data.put("value", filterCase.getStringId());
//
//                data.put("allowedNets", "filter");
//                filterCaseRefData.put("filter_case", data)
//                dataService.setData(task, ImportHelper.populateDataset(filterCaseRefData));

//                taskService.finishTask(task, userService.getLoggedUser());
            } catch (TransitionNotExecutableException e) {
                e.printStackTrace();
            }

            if(netCheck.get()) importResultMessage = importResultMessage.concat("OK\n");
//            task = taskService.searchOne(qTask.transitionId.eq("view").and(qTask.caseId.eq(menuItemCase.getStringId())));

//            return task.getStringId() + "," + filterCase.getStringId();
            return task.getCaseId() + "," + filterCase.getStringId() + "," + item.getUseIcon().toString();
        }

//        public Map<String, String> createFilterMapEntry (String Id,boolean setParentId){
//            Map<String, String> entry = new HashMap<>();
//
//            if (setParentId) {
//                entry.put("type", "caseRef");
//                entry.put("value", "");
//                entry.put("allowedNets", "filter");
//            } else {
//                entry.put("type", "text");
//                entry.put("value", Id);
//            }
//            return entry;
//        }

        private MenuEntry createMenuEntryExportClass (Case menuItemCase)
        {
            Map<String, I18nString> allowedRoles = menuItemCase.getDataSet().get(ALLOWED_ROLES).getOptions();
            Map<String, I18nString> bannedRoles = menuItemCase.getDataSet().get(BANNED_ROLES).getOptions();

            List<MenuEntryRole> menuEntryRoleList = new ArrayList<>();

            if (allowedRoles != null && !allowedRoles.isEmpty()) {
                menuEntryRoleList.addAll(allowedRoles.keySet().stream().map(roleNet -> {
                    MenuEntryRole newMenuEntryRole = new MenuEntryRole();
                    newMenuEntryRole.setRoleImportId(roleNet.split(":")[0]);
                    newMenuEntryRole.setNetImportId(roleNet.split(":")[1]);
                    newMenuEntryRole.setAuthorizationType(AuthorizationType.ALLOWED);
                    return newMenuEntryRole;
                }).collect(Collectors.toList()));
            }

            if (bannedRoles != null && !bannedRoles.isEmpty()) {
                menuEntryRoleList.addAll(bannedRoles.keySet().stream().map(roleNet -> {
                    MenuEntryRole newMenuEntryRole = new MenuEntryRole();
                    newMenuEntryRole.setRoleImportId(roleNet.split(":")[0]);
                    newMenuEntryRole.setNetImportId(roleNet.split(":")[1]);
                    newMenuEntryRole.setAuthorizationType(AuthorizationType.BANNED);
                    return newMenuEntryRole;
                }).collect(Collectors.toList()));
            }

            MenuEntry exportMenuItem = new MenuEntry();
            exportMenuItem.setEntryName(menuItemCase.getDataSet().get(MENU_ITEM_NAME).toString());
            exportMenuItem.setUseIcon((Boolean) menuItemCase.getDataSet().get(USE_ICON).getValue());
//            exportMenuItem.setIconIdentifier(menuItemCase.getDataSet().get(ICON_IDENTIFIER).toString());
            if (!menuEntryRoleList.isEmpty()) exportMenuItem.setMenuEntryRoleList(menuEntryRoleList);

            return exportMenuItem;
        }

        @Override
        public Map<String, I18nString>  createMenuExportChoices(MultichoiceMapField availableEntries, List<Case> menuItemCases) {
            Map <String, I18nString> availableItems; //TODO skontrolovat ci to nebude menit povodne options, premenovat na creatAvailableEntriesChoices
            availableItems = menuItemCases.stream()
                    .collect(Collectors.toMap(Case::getStringId, v -> new I18nString((String)v.getDataSet().get("entry_default_name").getValue())));

            return availableItems;
        }

        @Override
        public Map<String, I18nString>  addSelectedEntriesToExport(MultichoiceMapField availableEntries, EnumerationMapField menusForExport, String menuIdentifier) {
            Map <String, I18nString> updatedOptions = new LinkedHashMap<>(menusForExport.getOptions());
            String menuCaseIds = "";
            if (availableEntries.getOptions().size() != 0) {
                for (String id: availableEntries.getValue()) {
                    menuCaseIds = menuCaseIds.concat(id + ",");

                updatedOptions.put(menuCaseIds, new I18nString(menuIdentifier));
                }
            }
            return updatedOptions;
        }

        private String inputStreamToString (InputStream is) throws IOException {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return sb.toString();
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