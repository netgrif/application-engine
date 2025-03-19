package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.core.petrinet.domain.I18nString;
import com.netgrif.core.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.core.petrinet.domain.dataset.FileField;
import com.netgrif.core.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.core.petrinet.domain.dataset.MultichoiceMapField;
import com.netgrif.core.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.core.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.IllegalMenuFileException;
import com.netgrif.core.workflow.domain.menu.MenuEntry;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Interface which provides methods for menu import and export.
 */

public interface IMenuImportExportService {

    Map<String, I18nString> createAvailableEntriesChoices(List<Case> menuItemCases);

    Map<String, I18nString> addSelectedEntriesToExport(MultichoiceMapField availableEntries, EnumerationMapField menusForExport, String menuidentifier);

    FileFieldValue exportMenu(EnumerationMapField menusForExport, String groupId, FileField fileField) throws IOException;

    List<String> importMenu(List<Case> menuItemCases, FileFieldValue ffv, String groupCaseId) throws IOException, IllegalMenuFileException, TransitionNotExecutableException;

    String createMenuItemCase(StringBuilder resultMessage, MenuEntry item, String menuIdentifier, String groupCaseId, String filterCaseId);
}
