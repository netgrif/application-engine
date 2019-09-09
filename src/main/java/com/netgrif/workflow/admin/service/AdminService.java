package com.netgrif.workflow.admin.service;

import com.netgrif.workflow.admin.AdminConsoleRunner;
import com.netgrif.workflow.admin.service.interfaces.IAdminService;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.netgrif.workflow.workflow.web.responsebodies.MessageResource.successMessage;

@Service
public class AdminService implements IAdminService {

    @Autowired
    private AdminConsoleRunner actionsRunner;

    @Override
    public MessageResource runCode(String code) {
        actionsRunner.run(code);
        return successMessage("OK");
    }
}
