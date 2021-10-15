package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.FileField;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceMapField;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.MenuEntry;
import com.netgrif.workflow.workflow.domain.MenuList;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IMenuImportExport {

    Map<String, I18nString>  createMenuExportChoices(MultichoiceMapField availableEntries, List<Case> menuItemCases);
    Map<String, I18nString>  addSelectedEntriesToExport(MultichoiceMapField availableEntries, EnumerationMapField menusForExport, String menuidentifier);
    FileFieldValue exportMenu(EnumerationMapField menusForExport, String groupId, FileField fileField) throws IOException;
    List<String> importMenu(List<Case> menuItemCases, FileFieldValue ffv, String groupCaseId) throws IOException;
    String createMenuItemCase(MenuEntry item,  String menuIdentifier, String groupCaseId, String filterCaseId);
}
