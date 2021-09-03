package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.workflow.domain.Case;

import java.io.IOException;
import java.util.List;

public interface IMenuImportExport {

    void createMenuImport(User author);
    void createMenuExport(User author);
    FileFieldValue exportMenu(List<Case> menuItemCaseIds) throws IOException;
}
