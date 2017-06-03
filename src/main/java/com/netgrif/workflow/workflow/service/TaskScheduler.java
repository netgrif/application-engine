package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.workflow.service.interfaces.ITaskScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Service
public class TaskScheduler implements ITaskScheduler {

    @Autowired
    private org.springframework.scheduling.TaskScheduler scheduler;

    public ScheduledFuture schedule(Runnable task, Trigger trigger) {
        return scheduler.schedule(task, trigger);
    }

    public ScheduledFuture schedule(Runnable task, Date startTime) {
        return scheduler.schedule(task, startTime);
    }
}