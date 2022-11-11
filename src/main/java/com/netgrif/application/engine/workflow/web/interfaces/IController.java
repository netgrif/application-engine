package com.netgrif.application.engine.workflow.web.interfaces;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.slf4j.Logger;

public interface IController {

    IUserService userService();

    Logger log();
}
