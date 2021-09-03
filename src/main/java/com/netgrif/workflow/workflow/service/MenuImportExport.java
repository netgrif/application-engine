package com.netgrif.workflow.workflow.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.common.collect.Lists;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.importer.model.AuthorizationType;
import com.netgrif.workflow.importer.model.MenuItem;
import com.netgrif.workflow.importer.model.MenuItemRole;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.startup.DefaultFiltersRunner;
import com.netgrif.workflow.workflow.domain.*;
import com.netgrif.workflow.workflow.service.interfaces.IMenuImportExport;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuImportExport implements IMenuImportExport{

        private static final Logger log = LoggerFactory.getLogger(MenuImportExport.class);

        private static final String EXPORT_NET_IDENTIFIER = "export_menu";
        private static final String IMPORT_NET_IDENTIFIER = "import_menu";
        private static final String MENU_ITEM_NAME = "entry_name";
        private static final String USE_ICON = "use_icon";
        private static final String ICON_IDENTIFIER = "icon_name";
        private static final String ALLOWED_ROLES = "allowed_roles";
        private static final String BANNED_ROLES = "banned_roles";

        private static final String UPLOAD_FILE_FIELD = "upload_file";

        private static final String DEFAULT_SEARCH_CATEGORIES = "defaultSearchCategories";
        private static final String INHERIT_ALLOWED_NETS = "inheritAllowedNets";

        private static final String FILTER_TYPE_CASE = "Case";
        private static final String FILTER_TYPE_TASK = "Task";

        private static final String IMPORT_FILTER_TRANSITION = "import_filter";

        private static final String FIELD_VISIBILITY = "visibility";
        private static final String FIELD_FILTER_TYPE = "filter_type";
        private static final String FIELD_FILTER = "filter";
        private static final String FIELD_NAME = "i18n_filter_name";

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
        private FileStorageConfiguration fileStorageConfiguration;

        @Override
        public void createMenuImport(User author) {
            createFilterCase("IMP_" + author.getFullName(), IMPORT_NET_IDENTIFIER, author.transformToLoggedUser());
        }

        @Override
        public void createMenuExport(User author) {
            createFilterCase("EXP_" + author.getFullName(), EXPORT_NET_IDENTIFIER, author.transformToLoggedUser());
        }

        private void createFilterCase(String title, String netIdentifier, LoggedUser loggedUser) {
            PetriNet filterImportNet = petriNetService.getNewestVersionByIdentifier(netIdentifier);
            workflowService.createCase(filterImportNet.getStringId(), title, "", loggedUser);
        }

        @Override
        public FileFieldValue exportMenu(List<Case> menuItemCases) throws IOException {
            log.info("Exporting menu");
//            List<Case> menuItemCases = this.workflowService.findAllById(Lists.newArrayList(menuItemCaseIds));
            Menu menu = new Menu(new ArrayList<>());
            menuItemCases.forEach(menuItem -> {
                menu.getMenuItemList().add(createExportClass(menuItem));
            });
            return createXML(menu);
        }

//        @Override
//        public List<String> importFilters() throws IOException {
//            FilterImportExportList filterList = loadFromXML();
//            List<String> importedFiltersIds = new ArrayList<>();
//
//            filterList.getFilters().forEach(filter -> {
//                Optional<Case> filterCase = Optional.empty();
//                if (filter.getType().equals(FILTER_TYPE_CASE)) {
//                    filterCase = defaultFiltersRunner.createCaseFilter(
//                            filter.getFilterName().getDefaultValue(),
//                            filter.getIcon(),
//                            "",
//                            filter.getVisibility(),
//                            filter.getFilterValue(),
//                            filter.getAllowedNets(),
//                            filter.getFilterMetadata(),
//                            filter.getFilterName().getTranslations(),
//                            (boolean) filter.getFilterMetadata().get(DEFAULT_SEARCH_CATEGORIES),
//                            (boolean) filter.getFilterMetadata().get(INHERIT_ALLOWED_NETS)
//                    );
//                } else if (filter.getType().equals(FILTER_TYPE_TASK)) {
//                    filterCase = defaultFiltersRunner.createTaskFilter(
//                            filter.getFilterName().getDefaultValue(),
//                            filter.getIcon(),
//                            "",
//                            filter.getVisibility(),
//                            filter.getFilterValue(),
//                            filter.getAllowedNets(),
//                            filter.getFilterMetadata(),
//                            filter.getFilterName().getTranslations(),
//                            (boolean) filter.getFilterMetadata().get(DEFAULT_SEARCH_CATEGORIES),
//                            (boolean) filter.getFilterMetadata().get(INHERIT_ALLOWED_NETS)
//                    );
//                }
//
//                if (filterCase.isPresent()) {
//                    Task importFilterTask = taskService.searchOne(QTask.task.transitionId.eq(IMPORT_FILTER_TRANSITION).and(QTask.task.caseId.eq(filterCase.get().getStringId())));
//                    importedFiltersIds.add(importFilterTask.getStringId());
//                }
//            });
//
//            return importedFiltersIds;
//        }

//        @Transactional
//        protected FilterImportExportList loadFromXML() throws IOException {
//            Case exportCase = workflowService.searchOne(
//                    QCase.case$.processIdentifier.eq(IMPORT_NET_IDENTIFIER)
//                            .and(QCase.case$.author.id.eq(userService.getLoggedUser().getId()))
//            );
//
//            FileFieldValue ffv = (FileFieldValue) exportCase.getDataSet().get(UPLOAD_FILE_FIELD).getValue();
//
//            File f = new File(ffv.getPath());
//            XmlMapper xmlMapper = new XmlMapper();
//            String xml = inputStreamToString(new FileInputStream(f));
//            FilterImportExportList filterList = xmlMapper.readValue(xml, FilterImportExportList.class);
//
//            filterList.getFilters().forEach(filter -> {
//                Object defaultSearchCategories = filter.getFilterMetadata().get(DEFAULT_SEARCH_CATEGORIES);
//                Object inheritAllowedNets = filter.getFilterMetadata().get(INHERIT_ALLOWED_NETS);
//
//                filter.getFilterMetadata().put(DEFAULT_SEARCH_CATEGORIES, defaultSearchCategories.equals("true"));
//                filter.getFilterMetadata().put(INHERIT_ALLOWED_NETS, inheritAllowedNets.equals("true"));
//
//                if (filter.getAllowedNets() == null) {
//                    filter.setAllowedNets(new ArrayList<>());
//                }
//            });
//
//            return filterList;
//        }

        @Transactional
        protected FileFieldValue createXML(Menu menu) throws IOException {
            String filePath = fileStorageConfiguration.getStoragePath() + "/menuExport/menu_" + userService.getLoggedUser().getName() + ".xml";
            File f = new File(filePath);
            f.getParentFile().mkdirs();

            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            xmlMapper.writeValue(baos, menu);

            FileOutputStream fos = new FileOutputStream(f);
            baos.writeTo(fos);

            return new FileFieldValue("menu_" + userService.getLoggedUser().getName() + ".xml", filePath);
        }

        private MenuEntry createExportClass(Case menuItemCase)
        {
            Map<String, I18nString> allowedRoles = menuItemCase.getDataSet().get(ALLOWED_ROLES).getOptions();
            Map<String, I18nString> bannedRoles = menuItemCase.getDataSet().get(BANNED_ROLES).getOptions();

            List<MenuItemRole> menuItemRoleList = new ArrayList<>();

            if(allowedRoles != null && !allowedRoles.isEmpty()) {
                menuItemRoleList.addAll(allowedRoles.keySet().stream().map(roleNet -> {
                    MenuItemRole newMenuItemRole = new MenuItemRole();
                    newMenuItemRole.setRoleImportId(roleNet.split(":")[0]);
                    newMenuItemRole.setNetImportId(roleNet.split(":")[1]);
                    newMenuItemRole.setAuthorizationType(AuthorizationType.ALLOWED);
                    return newMenuItemRole;
                }).collect(Collectors.toList()));
            }

            if(bannedRoles != null && !bannedRoles.isEmpty()) {
                menuItemRoleList.addAll(bannedRoles.keySet().stream().map(roleNet -> {
                    MenuItemRole newMenuItemRole = new MenuItemRole();
                    newMenuItemRole.setRoleImportId(roleNet.split(":")[0]);
                    newMenuItemRole.setNetImportId(roleNet.split(":")[1]);
                    newMenuItemRole.setAuthorizationType(AuthorizationType.BANNED);
                    return newMenuItemRole;
                }).collect(Collectors.toList()));
            }

            MenuEntry exportMenuItem = new MenuEntry();
            exportMenuItem.setEntry_name(menuItemCase.getDataSet().get(MENU_ITEM_NAME).toString());
            exportMenuItem.setUseIcon((Boolean) menuItemCase.getDataSet().get(USE_ICON).getValue());
            exportMenuItem.setFilterId("filterIdXYZ");
            exportMenuItem.setIconIdentifier(menuItemCase.getDataSet().get(ICON_IDENTIFIER).toString());
            if(!menuItemRoleList.isEmpty()) exportMenuItem.setMenuItemRoleList(menuItemRoleList);

            return exportMenuItem;
        }

        private String inputStreamToString(InputStream is) throws IOException {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return sb.toString();
        }
}
