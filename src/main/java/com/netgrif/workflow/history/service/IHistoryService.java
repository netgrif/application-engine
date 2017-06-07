package com.netgrif.workflow.history.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.history.domain.UserEventLog;
import org.springframework.data.domain.Page;

public interface IHistoryService {

    Page<UserEventLog> findAllByUser(User user);
}
