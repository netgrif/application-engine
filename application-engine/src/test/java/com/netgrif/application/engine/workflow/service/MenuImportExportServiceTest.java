package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.files.StorageResolverService;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileField;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.MultichoiceMapField;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Storage;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.DataField;
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuAndFilters;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private Case menuCase(String id, String defaultName) {
        Case useCase = mock(Case.class);
        when(useCase.getStringId()).thenReturn(id);
        when(useCase.getDataSet()).thenReturn(Map.of("entry_default_name", new DataField(defaultName)));
        return useCase;
    }
}
