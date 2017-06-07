package com.netgrif.workflow.history.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.history.domain.UserEventLog;
import com.netgrif.workflow.history.domain.UserEventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class HistoryService implements IHistoryService {

    @Autowired
    private UserEventLogRepository userEventLogRepository;

    @Override
    public Page<UserEventLog> findAllByUser(User user) {
        return userEventLogRepository.findAllByEmail(user.getEmail());
    }
}