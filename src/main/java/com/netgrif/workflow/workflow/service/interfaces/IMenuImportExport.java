package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.dataset.FileField;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.MenuEntry;
import com.netgrif.workflow.workflow.domain.MenuList;

import java.io.IOException;
import java.util.List;

public interface IMenuImportExport {

    void createMenuImport(User author);
    void createMenuExport(User author);
    FileFieldValue exportMenu(List<Case> menuItemCaseIds, String menuIdentifier, String groupId, FileField fileField) throws IOException;
    List<String> importMenu(List<Case> menuItemCases, FileFieldValue ffv, String groupCaseId) throws IOException;
    String createMenuItemCase(MenuEntry item,  String menuIdentifier, String resultMessage, String groupCaseId);
}
