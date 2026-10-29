package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.files.StorageResolverService;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.*;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.DataField;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.menu.Menu;
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuAndFilters;
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuEntry;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IFilterImportExportService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MenuImportExportServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void createAvailableEntriesChoicesUsesEntryDefaultName() {
        MenuImportExportService service = new MenuImportExportService();
        Case first = menuCase("case-1", "Inbox");
        Case second = menuCase("case-2", "Archive");

        Map<String, I18nString> choices = service.createAvailableEntriesChoices(List.of(first, second));

        assertEquals("Inbox", choices.get("case-1").getDefaultValue());
        assertEquals("Archive", choices.get("case-2").getDefaultValue());
    }

    @Test
    void addSelectedEntriesToExportKeepsExistingOptionsAndAppendsSelection() {
        MenuImportExportService service = new MenuImportExportService();
        MultichoiceMapField availableEntries = new MultichoiceMapField(Map.of(
                "case-1", new I18nString("Inbox"),
                "case-2", new I18nString("Archive")
        ));
        availableEntries.setValue(new LinkedHashSet<>(List.of("case-1", "case-2")));
        Map<String, I18nString> existingOptions = new LinkedHashMap<>();
        existingOptions.put("old-case,", new I18nString("old-menu"));
        EnumerationMapField menusForExport = new EnumerationMapField(existingOptions);

        Map<String, I18nString> updated = service.addSelectedEntriesToExport(availableEntries, menusForExport, "main-menu");

        assertEquals("old-menu", updated.get("old-case,").getDefaultValue());
        assertEquals("main-menu", updated.get("case-1,case-2,").getDefaultValue());
    }

    @Test
    void addSelectedEntriesToExportDoesNotChangeOptionsWhenChoicesAreEmpty() {
        MenuImportExportService service = new MenuImportExportService();
        MultichoiceMapField availableEntries = new MultichoiceMapField();
        availableEntries.setValue(new LinkedHashSet<>(List.of("case-1")));
        Map<String, I18nString> existingOptions = new LinkedHashMap<>();
        existingOptions.put("old-case,", new I18nString("old-menu"));
        EnumerationMapField menusForExport = new EnumerationMapField(existingOptions);

        Map<String, I18nString> updated = service.addSelectedEntriesToExport(availableEntries, menusForExport, "main-menu");

        assertEquals(existingOptions, updated);
    }

    @Test
    void createXmlWritesMenuFileThroughResolvedStorage() throws Exception {
        UserService userService = mock(UserService.class);
        StorageResolverService storageResolverService = mock(StorageResolverService.class);
        IStorageService storageService = mock(IStorageService.class);
        AbstractUser user = mock(AbstractUser.class);
        FileField fileField = new FileField();
        fileField.setImportId("menu_export");
        fileField.setStorage(new Storage("local"));
        Path target = tempDir.resolve("menu_AdminUser.xml");
        MenuImportExportService service = new MenuImportExportService();
        service.userService = userService;
        ReflectionTestUtils.setField(service, "storageResolverService", storageResolverService);
        when(userService.getLoggedUser()).thenReturn(user);
        when(user.getName()).thenReturn("Admin User");
        when(storageResolverService.resolve("local")).thenReturn(storageService);
        when(storageService.getPath("group-1", "menu_export", "menu_AdminUser.xml")).thenReturn(target.toString());

        FileFieldValue value = service.createXML(new MenuAndFilters(), "group-1", fileField);

        assertEquals("menu_AdminUser.xml", value.getName());
        assertEquals(target.toString(), value.getPath());
        assertTrue(Files.exists(target));
        String xml = Files.readString(target);
        assertTrue(xml.contains("<?xml"));
        assertTrue(xml.contains("menusWithFilters"));
    }

    @Test
    void importMenuSkipsReplacingMenuWhenFilterMappingIsMissing() throws Exception {
        IFilterImportExportService filterImportExportService = mock(IFilterImportExportService.class);
        IWorkflowService workflowService = mock(IWorkflowService.class);
        ITaskService taskService = mock(ITaskService.class);
        IDataService dataService = mock(IDataService.class);

        MenuImportExportService service = spy(new MenuImportExportService());
        ReflectionTestUtils.setField(service, "filterImportExportService", filterImportExportService);
        ReflectionTestUtils.setField(service, "workflowService", workflowService);
        ReflectionTestUtils.setField(service, "taskService", taskService);
        ReflectionTestUtils.setField(service, "dataService", dataService);

        MenuAndFilters menuAndFilters = new MenuAndFilters();
        Menu menu = new Menu();
        menu.setMenuIdentifier("nav-menu");
        MenuEntry entry1 = new MenuEntry();
        entry1.setEntryName("Entry 1");
        entry1.setFilterCaseId("filter-case-1");
        MenuEntry entry2 = new MenuEntry();
        entry2.setEntryName("Entry 2");
        entry2.setFilterCaseId("filter-case-2");
        menu.setMenuEntries(List.of(entry1, entry2));
        menuAndFilters.getMenuList().setMenus(List.of(menu));

        FileFieldValue ffv = new FileFieldValue();
        doReturn(menuAndFilters).when(service).loadFromXML(ffv);

        when(filterImportExportService.importFilters(any())).thenReturn(Map.of("filter-case-1", "task-1"));

        Case existingCase = mock(Case.class);
        when(existingCase.getStringId()).thenReturn("existing-case-id");
        when(existingCase.getDataSet()).thenReturn(Map.of("menu_identifier", new DataField("nav-menu")));

        Task groupNavTask = mock(Task.class);
        when(taskService.searchOne(any())).thenReturn(groupNavTask);
        Task importedFilterTask = mock(Task.class);
        when(importedFilterTask.getCaseId()).thenReturn("filter-case-id");
        when(taskService.findOne("task-1")).thenReturn(importedFilterTask);
        Case filterCase = mock(Case.class);
        when(workflowService.findOne("filter-case-id")).thenReturn(filterCase);

        List<String> result = service.importMenu(List.of(existingCase), ffv, "parent-group-id");

        assertTrue(result.isEmpty());
        verify(workflowService, never()).findOne("existing-case-id");
        verify(service, never()).createMenuItemCase(any(), any(), any(), any(), any());
    }

    @Test
    void importMenuReplacesMenuWhenAllFilterMappingsArePresent() throws Exception {
        IFilterImportExportService filterImportExportService = mock(IFilterImportExportService.class);
        IWorkflowService workflowService = mock(IWorkflowService.class);
        ITaskService taskService = mock(ITaskService.class);
        IDataService dataService = mock(IDataService.class);

        MenuImportExportService service = spy(new MenuImportExportService());
        ReflectionTestUtils.setField(service, "filterImportExportService", filterImportExportService);
        ReflectionTestUtils.setField(service, "workflowService", workflowService);
        ReflectionTestUtils.setField(service, "taskService", taskService);
        ReflectionTestUtils.setField(service, "dataService", dataService);

        MenuAndFilters menuAndFilters = new MenuAndFilters();
        Menu menu = new Menu();
        menu.setMenuIdentifier("nav-menu");
        MenuEntry entry1 = new MenuEntry();
        entry1.setEntryName("Entry 1");
        entry1.setFilterCaseId("filter-case-1");
        menu.setMenuEntries(List.of(entry1));
        menuAndFilters.getMenuList().setMenus(List.of(menu));

        FileFieldValue ffv = new FileFieldValue();
        doReturn(menuAndFilters).when(service).loadFromXML(ffv);

        when(filterImportExportService.importFilters(any())).thenReturn(Map.of("filter-case-1", "task-1"));

        Case existingCase = mock(Case.class);
        when(existingCase.getStringId()).thenReturn("existing-case-id");
        when(existingCase.getDataSet()).thenReturn(Map.of("menu_identifier", new DataField("nav-menu")));
        when(existingCase.getFieldValue("remove_option")).thenReturn(0);
        when(workflowService.findOne("existing-case-id")).thenReturn(existingCase);

        Task groupNavTask = mock(Task.class);
        Task removeViewTask = mock(Task.class);
        when(taskService.searchOne(any())).thenReturn(removeViewTask).thenReturn(groupNavTask);

        Task importedFilterTask = mock(Task.class);
        when(importedFilterTask.getCaseId()).thenReturn("filter-case-id");
        when(taskService.findOne("task-1")).thenReturn(importedFilterTask);
        Case filterCase = mock(Case.class);
        when(workflowService.findOne("filter-case-id")).thenReturn(filterCase);

        doReturn("new-case-id,filter-case-id,true").when(service).createMenuItemCase(any(), eq(entry1), eq("nav-menu"), eq("parent-group-id"), eq("task-1"));

        List<String> result = service.importMenu(List.of(existingCase), ffv, "parent-group-id");

        assertEquals(List.of("new-case-id,filter-case-id,true"), result);
        verify(workflowService).findOne("existing-case-id");
        verify(service).createMenuItemCase(any(), eq(entry1), eq("nav-menu"), eq("parent-group-id"), eq("task-1"));
    }

    private Case menuCase(String id, String defaultName) {
        Case useCase = mock(Case.class);
        when(useCase.getStringId()).thenReturn(id);
        when(useCase.getDataSet()).thenReturn(Map.of("entry_default_name", new DataField(defaultName)));
        return useCase;
    }
}
