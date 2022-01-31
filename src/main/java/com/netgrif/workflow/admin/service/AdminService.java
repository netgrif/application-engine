package com.netgrif.workflow.admin.service;

import com.netgrif.workflow.admin.AdminConsoleRunner;
import com.netgrif.workflow.admin.service.interfaces.IAdminService;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.event.events.user.AdminActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class AdminService implements IAdminService {

    public static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private AdminConsoleRunner actionsRunner;

    /**
     * @param code maximal length is 65535 bytes
     */
    @Override
    public String run(String code, LoggedUser user) throws AdminActionException {
        try {
            publisher.publishEvent(new AdminActionEvent(user, code));
            return actionsRunner.run(code);
        } catch (Exception e) {
            log.error("Admin console ERROR: ", e);
            throw new AdminActionException(e);
        }
    }
}
