package com.netgrif.workflow.history.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.history.domain.IUserEventLog;
import com.netgrif.workflow.history.domain.repository.UserEventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class HistoryService implements IHistoryService {

    @Autowired
    private UserEventLogRepository userEventLogRepository;

    @Override
    public Page<IUserEventLog> findAllByUser(Pageable pageable, User user) {
        return userEventLogRepository.findAllByEmail(pageable, user.getEmail());
    }
}