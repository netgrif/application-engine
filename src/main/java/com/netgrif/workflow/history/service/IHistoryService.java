package com.netgrif.workflow.history.service;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.history.domain.IUserEventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IHistoryService {

    Page<IUserEventLog> findAllByUser(Pageable pageable, IUser user);
}
