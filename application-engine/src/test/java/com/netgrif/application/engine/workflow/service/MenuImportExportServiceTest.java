package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.MultichoiceMapField;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.DataField;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuImportExportServiceTest {

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

    private Case menuCase(String id, String defaultName) {
        Case useCase = mock(Case.class);
        when(useCase.getStringId()).thenReturn(id);
        when(useCase.getDataSet()).thenReturn(Map.of("entry_default_name", new DataField(defaultName)));
        return useCase;
    }
}
