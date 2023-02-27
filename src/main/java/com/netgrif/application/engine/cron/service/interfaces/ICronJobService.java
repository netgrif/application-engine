package com.netgrif.application.engine.cron.service.interfaces;

import com.netgrif.application.engine.workflow.domain.Case;
import org.quartz.SchedulerException;

public interface ICronJobService {
    void createJob(Case useCase) throws SchedulerException;

    boolean deactivateJob(Case useCase) throws SchedulerException;
}
