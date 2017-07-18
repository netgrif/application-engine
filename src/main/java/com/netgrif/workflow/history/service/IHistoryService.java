package com.netgrif.workflow.history.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.history.domain.UserEventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IHistoryService {

    Page<UserEventLog> findAllByUser(Pageable pageable, User user);
}
